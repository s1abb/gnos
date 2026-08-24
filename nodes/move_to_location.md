# Move to Location Node

This node drives an agent's vehicle to one or more destination locations on the network. It supports three movement modes:

- **Single Location** — navigates to a single `Target_Location`, with optional passing locations.
- **Stockpile Manager** — selects the best source location from a `Stockpile_Manager_Node` based on a queue and travel time score, then navigates there.
- **List of Locations** — navigates sequentially through a list of locations, with an optional delay between stops.
- **Departure Schedule** — follows a timetabled schedule of destinations keyed to the agent's `Vehicle_Id`, tracking late departures.

All modes support optional baked physics, a `Fuel_Threshold` exit condition, departure logging, and lookahead via `Future_Locations`. The node exits via `Exit` on success, `Low_Fuel` if the vehicle drops below the fuel threshold, or `Failed` if a destination cannot be set.

---

## Helper: `get_baked_physics`

Retrieves stored baked physics data for a given `group` key. Uses `Baked_Physics_Manager` if one is configured, otherwise falls back to reading directly from the `Baked_Physics_Map` dictionary.

```java
func get_baked_physics(group) {
    if Baked_Physics_Manager != null {
      return Baked_Physics_Manager.get(group);
    } else {
      return Baked_Physics_Map[group];
    }
}
```

---

## Helper: `set_baked_physics`

Stores baked physics data for a given `group` key. Mirrors `get_baked_physics` — writes via `Baked_Physics_Manager` if available, otherwise writes directly to `Baked_Physics_Map`.

```java
func set_baked_physics(group, baked) {
  if Baked_Physics_Manager != null {
    Baked_Physics_Manager.set(group, baked);
  } else {
    Baked_Physics_Map[group] = baked;
  }
}
```

---

## Helper: `run_to_destination`

Runs the vehicle to its currently set destination. Before navigating, sets up a baked physics engine if `Baked_Physics` is enabled for the group. After navigating, clears the baked physics engine and optionally records the result to `Baked_Physics_Manager`.

If `Use_Fuel_Threshold` is enabled, uses `run_to_destination_charge` to halt navigation if the vehicle's fuel would drop below `Fuel_Threshold` — returning `false` in that case. Returns `true` on successful navigation.

```java
func run_to_destination()
{
  // Set up baked physics engine if configured for this group
  if Baked_Physics in ["Use", "Record", "Use & Record"] {
    baked = get_baked_physics(Baked_Physics_Group);
    baked = agent.vehicle.setup_baked_physics_engine(baked, Baked_Physics in ["Use", "Use & Record"], Baked_Physics in ["Record", "Use & Record"]);
    set_baked_physics(Baked_Physics_Group, baked);
  }
  
  success = true;
  // Check optional Fuel_Threshold parameter
  if Use_Fuel_Threshold {
    // Try to navigate to destination without going below Fuel_Threshold
    if agent.vehicle.run_to_destination_charge(Fuel_Threshold / 100, Do_Not_Slow_Down != true)
    {
      // Vehicle is below desired Fuel_Threshold
      success = false;
    }
  }
  else
  {
    // Navigate to Target_Location
    agent.vehicle.run_to_destination(Do_Not_Slow_Down != true);
  }
  
  // Clear baked physics engine after navigation
  baked = agent.vehicle.clear_baked_physics_engine();
  // Optionally record baked physics result to the manager
  if Baked_Physics in ["Record", "Use & Record"]  and Baked_Physics_Manager != null {
    Baked_Physics_Manager.record(Baked_Physics_Group);
  }
  
  // Whether Vehicle successfully navigated to Target_Location
  return success;
}
```

---

## Main Execution Logic

The node first initialises the `future_locations` list from the `Future_Locations` input (used by all modes to give the vehicle lookahead along the planned path), then branches on `Movement_Mode`.

```java
// Get future locations from node params (used in all modes)
future_locations = [];
if Future_Locations {
    future_locations = Future_Locations;
}
```

### Single Location / Stockpile Manager

Both modes navigate to a single target location and share the same destination-setting and navigation logic. In **Single Location** mode `target_location` is taken directly from `Target_Location`. In **Stockpile Manager** mode the best source is selected from `Stockpile_Manager_Node` by scoring each candidate on its queue depth, inbound traffic, and average travel time; the agent's properties are updated with the chosen source and accepted take quantity, and travel statistics are recorded after arrival.

For bidirectional rail vehicles, if the destination cannot be set in the current orientation the node reverses the component order and flips the vehicle before retrying.

```java
if Movement_Mode == "Single Location" or Movement_Mode == "Stockpile Manager"
{
  target_location = null;
  dispatch_time = simulation.now;
  if Movement_Mode == "Stockpile Manager" {
    capacity = Maximum_Take;
    suggestions = Stockpile_Manager_Node.Suggest_Next_Sources(capacity, 1);
    best_score = 100000;
    best_location = null;
    
    total_travel = 0;
    
    for source in suggestions {
      location = source.source;
      total_travel += location.AVG_Travel;
    }
    
    avg_travel = total_travel / suggestions.len();
    
    // Score each candidate source; lower score is better
    for source in suggestions {
        location = source.source;
        inbound = location.Dispatched;
        queue = location.Queueing;
        travel_time = location.AVG_Travel;
        load_time = 1;
    
        score = (queue + inbound / (avg_travel / travel_time)) * load_time;
    
        if score < best_score {
            best_score = score;
            best_location = location;
        }
    }
    target_location = getvar(best_location.id);
    take = Stockpile_Manager_Node.Accept(best_location.id, capacity);
    // Store chosen source and take quantity on the agent for downstream nodes
    agent.properties[Stockpile_Manager_Node.ID] = {
      "Source": best_location,
      "Take": take,
    };
    best_location.Dispatched++;
    
  } else {
    // Single location mode just uses the set target location
    target_location = Target_Location;
  }

  // Get passing locations from node params
  passing_locations = [];
  if Passing_Locations
  {
    for passing in Passing_Locations
    {
      passing_locations.push(passing.Location);
    }
  }
  
  // Try to set vehicle destination; if it fails and the vehicle is bidirectional,
  // reverse the component order and flip the vehicle then retry
  if !agent.vehicle.set_destination(target_location, passing_locations, future_locations) and agent.vehicle.bidirectional {
      components = agent.vehicle.get_components().to_reversed();
      agent.vehicle.remove_all_components();
      agent.vehicle.add_components(components);
      agent.vehicle.flip();
  }

  if agent.vehicle.set_destination(target_location, passing_locations, future_locations)
  {
    // Add to Total_Departures
    Total_Departures += 1;
    // Optionally log departure events
    if Enable_Logs
    {
      simulation.log_info({
        "Vehicle Id": agent.Vehicle_Id,
        "Destination": target_location.name,
      });
    }
    // Try to run to destination
    if run_to_destination() == false
    {
      // Vehicle is below desired Fuel_Threshold
      return Low_Fuel;
    }

    // Navigation succeeded
    
    // Update stockpile manager travel statistics
    if Movement_Mode == "Stockpile Manager" {
      source = agent.properties[Stockpile_Manager_Node.ID].Source;
      source.Dispatched--;
      source.Total_Travel += (simulation.now - dispatch_time) / 60;
      source.Total_Trucks++;
      source.AVG_Travel = source.Total_Travel / source.Total_Trucks;
    }
    
    return Exit;
  }
  else
  {
    // Failed to navigate to Target_Location
    return Failed;
  }
}
```

---

### List of Locations

Navigates sequentially through every entry in `Target_Locations`. The full list is pre-loaded into `future_locations` so the vehicle has lookahead for the entire route. At each stop the current location is removed from the front of `future_locations` before the destination is set. An optional `Delay_At_Stop` pause is applied between each leg. Returns `Low_Fuel`, `Failed`, or `Exit`.

```java
else if Movement_Mode == "List of Locations"
{
  // Add Target_Locations to beginning of future_locations list
  index = Target_Locations.len() - 1;
  while index >= 0
  {
    future_locations.insert(0, {"Location":Target_Locations[index].Location});
    index -= 1;
  }
  // Try to navigate to all Target_Locations
  for curr in Target_Locations
  {
    // Remove curr location from the beginning of future_locations
    future_locations.pop(0);
    // Try to set vehicle destination
    if agent.vehicle.set_destination(curr.Location, [], future_locations)
    {
      // Add to Total_Departures
      Total_Departures += 1;
      // Optionally log departure events
      if Enable_Logs
      {
        simulation.log_info({
          "Vehicle Id": agent.Vehicle_Id,
          "Destination": curr.Location.name,
        });
      }
      // Try to run to destination
      if run_to_destination() == false
      {
        // Vehicle is below desired Fuel_Threshold
        return Low_Fuel;
      }
    }
    else
    {
      // Failed to navigate to curr.Location
      return Failed;
    }
    // Wait at curr.Location before moving to the next location
    agent.delay(Delay_At_Stop);
  }
  // Vehicle successfully navigated to all Target_Locations
  return Exit;
}
```

---

### Departure Schedule

Follows a timetabled list of destinations from `Departure_Schedule`, filtered and sorted by `Departure_Time` for the agent's `Vehicle_Id`. The full schedule of destinations is pre-loaded into `future_locations` for lookahead. For each departure the node waits until the scheduled time (or proceeds immediately and flags it as late if the time has already passed), then navigates. Increments `Total_Departures` and `Late_Departures` as appropriate. Returns `Low_Fuel`, `Failed`, or `Exit` on schedule completion.

```java
else if Movement_Mode == "Departure Schedule"
{
  // Check if agent "Vehicle Id" parameter exists
  if agent.Vehicle_Id == null
  {
    // Throw error if agent "Vehicle Id" parameter is missing
    throw_error("Missing required agent parameter 'Vehicle Id'");
  }
  // Get schedule for curr Vehicle_Id
  schedule = Departure_Schedule.filter(func (entry) {
    return entry.Vehicle_Id == agent.Vehicle_Id;
  });
  // Sort schedule by Departure_Time
  schedule = schedule.sort(func (a, b) {
    if a.Departure_Time < b.Departure_Time
    {
      return -1;
    }
    if a.Departure_Time > b.Departure_Time
    {
      return 1;
    }
    return 0;
  });
  // Add scheduled destinations to beginning of future_locations list
  index = schedule.len() - 1;
  while index >= 0
  {
    // Get track feature for destination
    destination = getvar(sanitise_name(schedule[index].Destination));
    future_locations.insert(0, {"Location": destination});
    index -= 1;
  }
  // Try to run through scheduled departures
  for departure in schedule
  {
    // Check if train is late for departure
    late_departure = false;
    if departure.Departure_Time < simulation.current_time
    {
      // Train was late for departure
      late_departure = true;
    }
    else
    {
      // Wait until Departure_Time
      agent.delay_until_date_time(departure.Departure_Time);
    }
    // Remove curr departure destination from the beginning of future_locations
    destination = future_locations.pop(0).Location;
    // Try to set vehicle destination
    if agent.vehicle.set_destination(destination, [], future_locations)
    {
      // Add to Total_Departures
      Total_Departures += 1;
      // Add to Late_Departures
      if late_departure
      {
        Late_Departures += 1;
      }
      // Optionally log departure events
      if Enable_Logs
      {
        simulation.log_info({
          "Vehicle Id": agent.Vehicle_Id,
          "Destination": destination.name,
          "Late departure": late_departure
        });
      }
      // Try to run to destination
      if run_to_destination() == false
      {
        // Vehicle is below desired Fuel_Threshold
        return Low_Fuel;
      }
    }
    else
    {
      // Failed to navigate to curr.Location
      return Failed;
    }
  }
  // Departure schedule completed successfully
  return Exit;
}
```

---

## Fallback Exit

If `Movement_Mode` does not match any known value the node exits via `Failed`.

```java
// Failed to navigate due to invalid Movement_Mode
return Failed;
```
