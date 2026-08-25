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
if Name {
  agent.vehicle.set_name(Name);
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

// Send agent to the next node
return Exit;