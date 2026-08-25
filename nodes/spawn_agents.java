// Helper function to add vehicle to agent
func add_vehicle(agent) {
  func get_components(List) {
    components = [];
    loads = [];
    if (List) {
      for row in List {
        count = 0;
        while count < row.Quantity {
          components.push(row.Component);
          load = row["Load KG"];
          if (load == null) {
              load = 0;
          }
          loads.push(load);
          count++;
        }
      }
    }
    return {"components": components, "loads": loads};
  }

  Components = [];
  if Vehicle_Type == "Rail" {
    Components = Rail_Components;
  } else {
    Components = Vehicle_Components;
  }
  {components, loads} = get_components(Components);

  if Vehicle_Type == "Road"
  {
    // Create new road vehicle
    if Motor_Vehicle {
        components.insert(0, Motor_Vehicle);
        loads.insert(0,0);
    }
    agent.add_vehicle(Network_Model, "road", components);
    agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
  }
  else if Vehicle_Type == "Rail"
  {
    // Create new rail vehicle
    agent.add_vehicle(Network_Model, "rail", components);
    // Optionally track train component data
    agent.vehicle.set_track_component_data(Log_Components);
    // Set train bidirectional property
    agent.vehicle.bidirectional = Bidirectional;
  }
  else if Vehicle_Type == "No Physics (Full Interaction)"
  {
    // Create new generic vehicle
    agent.add_vehicle(Network_Model, "generic", components);
    agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
  }
  else if Vehicle_Type == "No Physics (Limited Interaction)"
  {
    // Create new generic-interpolation vehicle
    agent.add_vehicle(Network_Model, "generic-interpolation", components);
    agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
  }

  i = 0;
  for c in agent.vehicle.get_components() {
    if loads.len() > i {
      c.add_load(loads[i]);
    }
    i++;
  }
  
  // Configure vehicle using train component
  if Use_Train {
    agent.vehicle.set_train(Train_Component);
  }
  
  // Optionally set name of new vehicle
  if Vehicle_Name {
    agent.vehicle.set_name(Vehicle_Name);
  }
  
  // Optionally set vehicle's tags
  if Tags {
    for row in Tags {
      tag = Network_Model.get_tag(row["Tag Name"]);
      if tag {
        agent.vehicle.add_tag(tag);
      }
    }
  }
  
  // Optionally place agent on track
  if Track_Feature
  {
    // Try to place agent vehicle at specified track feature
    while !agent.vehicle.place(Track_Feature)
    {
      // Try again after 1 second delay
      agent.delay(1);
    }
  }
  
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

  if Set_Controller {
    if Controller_Type == "PID" {
      agent.vehicle.engine.set_controller("PID");
      if KP != null {
        agent.vehicle.engine.controller.kp = KP;
      }
      if KI != null {
        agent.vehicle.engine.controller.ki = KI;
      }
      if KD != null {
        agent.vehicle.engine.controller.kd = KD;
      }
    }
    if Controller_Type == "MRAC" {
      agent.vehicle.engine.set_controller("MRAC");
      if Rising_Time != null {
        agent.vehicle.engine.controller.rising_time = Rising_Time;
        agent.vehicle.engine.controller.adapt();
      }
    }
  }
}

// Helper function to spawn agents
func spawn_agents(spawn_count, properties)
{
  num_spawned = 0;
  // Spawn agents if conditions are met
  while Allow_Spawn and num_spawned < spawn_count and (Maximum_Count == null or Total_Spawned < Maximum_Count)
  {
    // Spawn new agent
    new_agent = simulation.spawn(Agent_Type, Exit, !Spawn_Vehicle);
    if Spawn_Vehicle {
      add_vehicle(new_agent);
      simulation.run(new_agent);
    }
    
    // Set optional agent properties
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
  // Optionally log spawn event
  if Enable_Logs
  {
    simulation.log_info(stringify(num_spawned) + " '" + Agent_Type.name + "' agents spawned");
  }
}


// Flag to trigger termination of the running node
terminate = false;


if Spawn_Mode == "Once"
{
  // Trigger spawn event once
  spawn_agents(Number, null);
  // Terminate the running node
  terminate = true;
}


else if Spawn_Mode == "Interval"
{
  // Make sure Interval is greater than 0
  if (Interval <= 0)
  {
    throw_error("Interval must be greater than 0");
  }
  // Optionally trigger spawn event at start of simulation
  if Spawn_At_Start and simulation.now == 0
  {
    spawn_agents(Number, null);
  }
  // Delay until next spawn event
  node.delay(Interval);
  // Trigger spawn event after interval
  spawn_agents(Number, null);
}


else if Spawn_Mode == "Rate"
{
  // Make sure Rate is greater than 0
  if (Rate <= 0)
  {
    throw_error("Rate must be greater than 0");
  }
  // Optionally trigger spawn event at start of simulation
  if Spawn_At_Start and simulation.now == 0
  {
    spawn_agents(Number, null);
  }
  // Delay until next spawn event
  node.delay(1 / Rate);
  // Trigger spawn event at constant rate
  spawn_agents(Number, null);
}


else if Spawn_Mode == "Schedule"
{
  datetime_column = "Spawn_Datetime";
  number_column = "Spawn_Number";
  // Initialise schedule
  if Sorted_Schedule == null
  {
    // Check spawn schedule exists
    if Spawn_Schedule == null
    {
      throw_error("Missing spawn schedule");
    }
    // Get simulation start time
    start_time = simulation.current_time;
    // Remove scheduled events that occur before the simulation start time
    Sorted_Schedule = Spawn_Schedule.filter(func (event) {
      return start_time <= event[datetime_column];
    });
    // Sort schedule by spawn event datetime
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
  // Get next scheduled event
  scheduled_event = Sorted_Schedule[Schedule_Index];
  // Get datetime of next scheduled spawn event
  spawn_datetime = scheduled_event[datetime_column];
  // Get the number of agents to spawn
  spawn_number = scheduled_event[number_column];
  if spawn_number == null or spawn_number == 0
  {
    spawn_number = Number;
  }
  // Delay until next spawn event
  node.delay_until_date_time(spawn_datetime);
  // Get any extra columns on the scheduled_event to use as extra agent properties
  properties = {};
  for key in scheduled_event
  {
    // Skip datetime and number columns
    if key == datetime_column or key == number_column
    {
      continue;
    }
    properties[key] = scheduled_event[key];
  }
  // Trigger spawn event at scheduled datetime
  spawn_agents(spawn_number, properties);
  // Move on to the next scheduled event
  Schedule_Index++;
  // Terminate running node once schedule completed
  if (Schedule_Index >= Sorted_Schedule.len())
  {
    terminate = true;
  }
}


// Check if node should be terminated
if (terminate)
{
  node.terminate();
}

// Yield execution to spawned agent executors
node.delay(0);

node.allow_loop_no_delay();