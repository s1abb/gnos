# Add Vehicle Node

This node attaches a vehicle to an agent and configures it based on the `Vehicle_Type` input. It supports four vehicle types:

- **Road** — creates a road vehicle from a list of components, with an optional motor vehicle prepended.
- **Rail** — creates a rail vehicle, with optional train component logging and bidirectional travel support.
- **No Physics (Full Interaction)** — creates a generic vehicle with full collision interaction but no physics simulation.
- **No Physics (Limited Interaction)** — creates a generic interpolation vehicle with limited interaction and no physics simulation.

After creating the vehicle, the node optionally applies a train component, assigns a display name, adds tags, places the vehicle on a track feature, and configures PID and motion control parameters. The node exits via the `Exit` output to route the agent to the next node.

---

## Helper: `get_components`

Builds a flat list of vehicle components from a quantity-component table. Each row in `List` specifies a `Component` and a `Quantity`; the component is appended to the output list that many times. Returns an empty list if `List` is null or empty.

```java
func get_components(List) {
  components = [];
  if (List) {
    for row in List {
      count = 0;
      // Repeat the component entry Quantity times
      while count < row.Quantity {
        components.push(row.Component);
        count++;
      }
    }
  }
  return components;
}
```

---

## Main Execution Logic

The node branches on the `Vehicle_Type` input to create the appropriate vehicle, then applies a series of optional configuration blocks that are shared across all vehicle types.

### Road

Creates a road vehicle from the `Vehicle_Components` table. If `Motor_Vehicle` is provided it is inserted at the front of the component list. Sets the `turn_on_spot` capability from the `Can_Turn_On_Spot` input.

```java
if Vehicle_Type == "Road"
{
  // Build component list and prepend motor vehicle if provided
  components = get_components(Vehicle_Components);
  if Motor_Vehicle {
    components.insert(0, Motor_Vehicle);
  }
  agent.add_vehicle(Network_Model, "road", components);
  agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
}
```

---

### Rail

Creates a rail vehicle from the `Rail_Components` table. Optionally records per-component tracking data and sets whether the vehicle can travel in both directions along the track.

```java
else if Vehicle_Type == "Rail"
{
  // Create new rail vehicle
  agent.add_vehicle(Network_Model, "rail", get_components(Rail_Components));
  // Optionally track train component data
  agent.vehicle.set_track_component_data(Log_Components);
  // Set train bidirectional property
  agent.vehicle.bidirectional = Bidirectional;
}
```

---

### No Physics (Full Interaction)

Creates a generic vehicle with full collision interaction but no physics simulation. Component list and `turn_on_spot` behave the same as the Road type.

```java
else if Vehicle_Type == "No Physics (Full Interaction)"
{
  // Create new generic vehicle
  agent.add_vehicle(Network_Model, "generic", get_components(Vehicle_Components));
  agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
}
```

---

### No Physics (Limited Interaction)

Creates a generic interpolation vehicle with limited interaction and no physics simulation. Suitable for agents that need to move along a path without full collision processing.

```java
else if Vehicle_Type == "No Physics (Limited Interaction)"
{
  // Create new generic-interpolation vehicle
  agent.add_vehicle(Network_Model, "generic-interpolation", get_components(Vehicle_Components));
  agent.vehicle.turn_on_spot = Can_Turn_On_Spot;
}
```

---

## Vehicle Configuration

After the vehicle is created, a series of optional configuration blocks are applied regardless of vehicle type. These are all gated on their respective input values being set.

```java
// Configure vehicle using train component
if Use_Train {
  agent.vehicle.set_train(Train_Component);
}

// Optionally set name of new vehicle
if Name {
  agent.vehicle.set_name(Name);
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

// Optionally place agent vehicle at a specified track feature,
// retrying every second until placement succeeds
if Track_Feature
{
  while !agent.vehicle.place(Track_Feature)
  {
    // Try again after 1 second delay
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
```

---

## Exit

Routes the agent to the next node via the `Exit` output.

```java
// Send agent to the next node
return Exit;
```
