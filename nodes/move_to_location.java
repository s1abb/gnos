// Helper function to run vehicle to destination
// returns true on successful navigation or false if below Fuel_Threshold
func get_baked_physics(group) {
    if Baked_Physics_Manager != null {
      return Baked_Physics_Manager.get(group);
    } else {
      return Baked_Physics_Map[group];
    }
}

func set_baked_physics(group, baked) {
  if Baked_Physics_Manager != null {
    Baked_Physics_Manager.set(group, baked);
  } else {
    Baked_Physics_Map[group] = baked;
  }
}

func run_to_destination()
{
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
  
  baked = agent.vehicle.clear_baked_physics_engine();
  if Baked_Physics in ["Record", "Use & Record"]  and Baked_Physics_Manager != null {
    Baked_Physics_Manager.record(Baked_Physics_Group);
  }
  
  // Whether Vehicle succsessfully navigated to Target_Location
  return success;
}


// Get future locations from node params (used in all modes)
future_locations = [];
if Future_Locations {
    future_locations = Future_Locations;
}


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
  
  // Try to set vehicle destination
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
    Delay_At_Stop = curr["Delay At Stop Seconds"];
    if (Delay_At_Stop == null) {
      Delay_At_Stop = Default_Delay_At_Stop;
    }
    agent.delay(Delay_At_Stop);
  }
  // Vehicle succsessfully navigated to all Target_Locations
  return Exit;
}


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

// Failed to navigate due to invalid Movement_Mode
return Failed;