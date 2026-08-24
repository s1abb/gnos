
# Spawn Agents Node

This node spawns agents into the simulation according to a configured spawn mode. It supports four modes:

- **Once** — spawns a fixed number of agents a single time then terminates.
- **Interval** — spawns agents repeatedly on a fixed time interval.
- **Rate** — spawns agents at a constant rate (agents per second).
- **Schedule** — spawns agents at specific datetimes defined in a schedule table.

Optionally, each spawned agent can be assigned a vehicle (road, rail, or physics-free), configured with PID and control parameters, placed on a track feature, and tagged.

---

## Helper: `add_vehicle`

Attaches and configures a vehicle to a newly spawned agent based on the `Vehicle_Type` input. Handles road, rail, and non-physics vehicle types. Also supports optional PID tuning, movement control limits, track placement, and tagging.

```java
// Helper function to add vehicle to agent
func add_vehicle(agent) {

  // Build a flat list of components from a quantity-component table
  func get_components(List) {
    components = [];
    if (List) {
      for row in List {
        count = 0;
        while count < row.Quantity {
          components.push(row.Component);
          count++;
        }
      }
    }
    return components;
  }

  // Create the appropriate vehicle type based on the Vehicle_Type input
  if Vehicle_Type == "Road"
  {
    // Create new road vehicle, prepending the motor vehicle component if provided
    components = get_components(Vehicle_Components);
    if Motor_Vehicle {
      components.insert(0, Motor_Vehicle);
    }
    agent.add_vehicle(Network_Model, "road", components);
    agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
  }
  else if Vehicle_Type == "Rail"
  {
    // Create new rail vehicle
    agent.add_vehicle(Network_Model, "rail", get_components(Rail_Components));
    // Optionally track train component data
    agent.vehicle.set_track_component_data(Log_Components);
    // Set train bidirectional property
    agent.vehicle.bidirectional = Bidirectional;
  }
  else if Vehicle_Type == "No Physics (Full Interaction)"
  {
    // Create generic vehicle with full collision interaction
    agent.add_vehicle(Network_Model, "generic", get_components(Vehicle_Components));
    agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
  }
  else if Vehicle_Type == "No Physics (Limited Interaction)"
  {
    // Create generic interpolation vehicle with limited interaction
    agent.add_vehicle(Network_Model, "generic-interpolation", get_components(Vehicle_Components));
    agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
  }

  // Optionally attach a train component to the vehicle
  if Use_Train {
    agent.vehicle.set_train(Train_Component);
  }

  // Optionally assign a display name to the vehicle
  if Vehicle_Name {
    agent.vehicle.set_name(Vehicle_Name);
  }

  // Optionally apply tags to the vehicle from the Tags table
  if Tags {
    for row in Tags {
      tag = Network_Model.get_tag(row["Tag Name"]);
      if tag {
        agent.vehicle.add_tag(tag);
      }
    }
  }

  // Optionally place the agent vehicle at a specified track feature,
  // retrying every second until placement succeeds
  if Track_Feature
  {
    while !agent.vehicle.place(Track_Feature)
    {
      agent.delay(1);
    }
  }

  // Optionally configure PID controller gains
  if Setup_PID {
    if KP != null {
      agent.vehicle.engine.pid.kp = KP;
    }
    if KI != null {
      agent.vehicle.engine.pid.ki = KI;
    }
    if KD != null {
      agent.vehicle.engine.pid.kd = KD;
    }
  }

  // Optionally configure vehicle motion control limits
  if Setup_Control {
    if Max_Acceleration != null {
      agent.vehicle.set_max_acceleration(Max_Acceleration);
    }
    if Max_Braking != null {
      agent.vehicle.set_max_braking(Max_Braking);
    }
    if Max_Force != null {
      agent.vehicle.set_max_force(Max_Force);
    }
    if Max_Speed != null {
      agent.vehicle.set_max_speed(Max_Speed);
    }
    if Safe_Boundary != null {
      agent.vehicle.set_safe_boundary(Safe_Boundary);
    }
  }
}
```

---

## Helper: `spawn_agents`

Spawns up to `spawn_count` agents of type `Agent_Type`, subject to the `Allow_Spawn` flag and the optional `Maximum_Count` cap. Attaches a vehicle to each agent if `Spawn_Vehicle` is enabled, and applies any additional key-value properties passed in. Optionally logs the spawn event.

```java
// Helper function to spawn a batch of agents
func spawn_agents(spawn_count, properties)
{
  num_spawned = 0;

  // Spawn agents while conditions allow
  while Allow_Spawn and num_spawned < spawn_count and (Maximum_Count == null or Total_Spawned < Maximum_Count)
  {
    // Spawn a new agent at the configured exit point
    new_agent = simulation.spawn(Agent_Type, Exit, !Spawn_Vehicle);

    // Attach and start the vehicle if enabled
    if Spawn_Vehicle {
      add_vehicle(new_agent);
      simulation.run(new_agent);
    }

    // Apply any extra properties (e.g. from a schedule row) to the agent
    if properties
    {
      for key in properties
      {
        new_agent.properties[key] = properties[key];
      }
    }

    num_spawned++;
    Total_Spawned++;
    node.exited++;
  }

  // Optionally log how many agents were spawned in this batch
  if Enable_Logs
  {
    simulation.log_info(stringify(num_spawned) + " '" + Agent_Type.name + "' agents spawned");
  }
}
```

---

## Main Execution Logic

The node's behaviour depends on the `Spawn_Mode` input. A `terminate` flag is used to signal when the node should stop looping.

```java
// Flag to trigger termination of the running node
terminate = false;
```

### Once

Spawns the configured number of agents a single time, then terminates the node.

```java
if Spawn_Mode == "Once"
{
  spawn_agents(Number, null);
  terminate = true;
}
```

---

### Interval

Spawns agents repeatedly, pausing for a fixed `Interval` (in seconds) between each batch. Optionally spawns an initial batch at simulation time zero.

```java
else if Spawn_Mode == "Interval"
{
  // Interval must be a positive value
  if (Interval <= 0)
  {
    throw_error("Interval must be greater than 0");
  }

  // Optionally spawn at simulation start
  if Spawn_At_Start and simulation.now == 0
  {
    spawn_agents(Number, null);
  }

  // Wait for the configured interval, then spawn
  node.delay(Interval);
  spawn_agents(Number, null);
}
```

---

### Rate

Spawns agents at a constant rate (batches per second). Equivalent to Interval mode with `Interval = 1 / Rate`.

```java
else if Spawn_Mode == "Rate"
{
  // Rate must be a positive value
  if (Rate <= 0)
  {
    throw_error("Rate must be greater than 0");
  }

  // Optionally spawn at simulation start
  if Spawn_At_Start and simulation.now == 0
  {
    spawn_agents(Number, null);
  }

  // Delay inversely proportional to the rate, then spawn
  node.delay(1 / Rate);
  spawn_agents(Number, null);
}
```

---

### Schedule

Spawns agents at specific datetimes defined in the `Spawn_Schedule` table. On first execution the schedule is filtered to remove past events and sorted by datetime. Each loop iteration advances to the next scheduled event and terminates the node once all events have been processed.

Extra columns in the schedule row (beyond `Spawn_Datetime` and `Spawn_Number`) are passed to spawned agents as additional properties.

```java
else if Spawn_Mode == "Schedule"
{
  datetime_column = "Spawn_Datetime";
  number_column = "Spawn_Number";

  // Initialise and sort the schedule on first run
  if Sorted_Schedule == null
  {
    if Spawn_Schedule == null
    {
      throw_error("Missing spawn schedule");
    }

    // Filter out events that have already passed
    start_time = simulation.current_time;
    Sorted_Schedule = Spawn_Schedule.filter(func (event) {
      return start_time <= event[datetime_column];
    });

    // Sort remaining events in ascending datetime order
    Sorted_Schedule = Sorted_Schedule.sort(func (a, b) {
      if a[datetime_column] < b[datetime_column]
      {
        return -1;
      }
      else if a[datetime_column] > b[datetime_column]
      {
        return 1;
      }
      return 0;
    });
  }

  // Retrieve the next scheduled event
  scheduled_event = Sorted_Schedule[Schedule_Index];
  spawn_datetime = scheduled_event[datetime_column];

  // Use the row's spawn number, falling back to the default Number input
  spawn_number = scheduled_event[number_column];
  if spawn_number == null or spawn_number == 0
  {
    spawn_number = Number;
  }

  // Wait until the scheduled spawn datetime
  node.delay_until_date_time(spawn_datetime);

  // Collect any extra columns as agent properties (skip the reserved columns)
  properties = {};
  for key in scheduled_event
  {
    if key == datetime_column or key == number_column
    {
      continue;
    }
    properties[key] = scheduled_event[key];
  }

  // Spawn the batch with any associated properties
  spawn_agents(spawn_number, properties);

  // Advance the schedule index
  Schedule_Index++;

  // Terminate the node once all scheduled events have been processed
  if (Schedule_Index >= Sorted_Schedule.len())
  {
    terminate = true;
  }
}
```

---

## Termination & Loop Control

```java
// Terminate the node if flagged (e.g. after Once mode or end of schedule)
if (terminate)
{
  node.terminate();
}

// Yield execution to allow spawned agent executors to run
node.delay(0);

// Allow the node to loop without requiring a minimum delay
node.allow_loop_no_delay();
```