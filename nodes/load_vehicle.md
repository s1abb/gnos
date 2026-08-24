# Load Vehicle Node

This node loads material onto the components of an agent's vehicle. It first selects which components to load using `Selection_Method`, then determines how much to load per component using `Load_Method`. Loading simulates the time taken to transfer material, optionally drawing from a flow simulation container.

Component selection supports three methods:

- **Type** — selects components matching a specified `Component_Type`.
- **Index** — selects a slice of components between `Start_Index` and `End_Index`.
- **All** — selects every component on the vehicle.

Load amount supports five methods:

- **Load Per Component** — loads a fixed amount onto each component.
- **Shared Amount** — divides `Max_Shared_Amount` evenly across all selected components.
- **Target Amount** — loads up to a total `Target_Amount` across all components, stopping early once reached.
- **Fully Load** — fills each component to its full remaining capacity.
- **Stockpile Manager** — seizes the source stockpile resource, calculates load per component from the accepted take quantity, then releases the resource after loading.

The node exits immediately via `Exit` if no components are selected or if the calculated load per component is zero. On completion it increments `Total_Loaded` and exits via `Exit`.

---

## Main Execution Logic

### Component Selection

Builds the list of vehicle components to load based on `Selection_Method`. Exits immediately if no components are found.

```java
// Get wagons
components = [];
if Selection_Method == "Type"
{
  components = agent.vehicle.get_components_filtered(Component_Type);
}
else if Selection_Method == "Index"
{
  components = agent.vehicle.get_components().slice(Start_Index - 1, End_Index - 1);
}
else if Selection_Method == "All"
{
  components = agent.vehicle.get_components();
}

// No components to load, continue to next node
if components.len() == 0 {
  return Exit;
}
```

---

### Load Amount Calculation

Determines the maximum amount to load per component from `Load_Method`. For **Stockpile Manager** mode, the agent's pre-stored source data (written by a prior Move to Location node) is used to seize the source resource before loading begins. Exits immediately if the calculated load per component is zero.

```java
// Determine maximum load per component
max_load_per_component = null;
target_load = null;
if Load_Method == "Load Per Component"
{
  max_load_per_component = Load_Per_Component;
}
else if Load_Method == "Shared Amount"
{
  max_load_per_component = Max_Shared_Amount / components.len();
}
else if Load_Method == "Target Amount"
{
  target_load = Target_Amount;
}
else if Load_Method == "Stockpile Manager"
{
  // Seize source stockpile resource
  data = agent.properties[Stockpile_Manager_Node.ID];
  data.Source.Queueing++;
  agent.seize_resource(data.Source.Resource);
  data.Source.Queueing--;
  max_load_per_component = (data.Take * 1000) / components.len();
}

// Nothing to load, continue to next node
if max_load_per_component == 0
{
  return Exit;
}
```

---

### Loading Loop

Iterates over each selected component, calculates the load amount for that component, simulates the transfer duration, and applies the load. For **Target Amount** mode, loading stops as soon as the cumulative loaded amount reaches `target_load`.

The transfer duration is determined by `Load_Rate` unless `Use_Load_Duration` is set, in which case the rate is derived from `Load_Component_Duration`. If a `Source_Container` is configured, material is drawn from a flow simulation container; otherwise the delay is simulated directly.

```java
// Track total loaded amount
loaded = 0;

// Load every selected component
for component in components {
  // Calculate load amount
  load_amount = max_load_per_component;
  remaining_capacity = max([component.capacity - component.current_load, 0]);
  if Load_Method == "Fully Load"
  {
    load_amount = remaining_capacity;
  }
  else if Load_Method == "Target Amount"
  {
    // Ensure we do not load more than target amount
    if loaded + remaining_capacity > target_load
    {
      load_amount = target_load - loaded;
    }
    else
    {
      load_amount = remaining_capacity;
    }
  }

  // Nothing to load, continue to next component
  if load_amount == 0
  {
    continue;
  }

  // Calculate load rate
  rate = Load_Rate;
  if Use_Load_Duration
  {
    rate = load_amount / Load_Component_Duration;
  }

  if Source_Container
  {
    // Take from flow sim container
    c = Source_Container.create_output_container(0, load_amount, rate);
    agent.delay(c.delay_until_full());
    c.remove();
  }
  else
  {
    // Simulate time it would take to transfer material
    agent.delay(load_amount / rate);
  }

  // Add load to component
  component.add_load(load_amount);

  // Sum loaded amount
  loaded += load_amount;

  // If reached target amount stop loading and continue to next node
  if Load_Method == "Target Amount" and loaded >= target_load
  {
    break;
  }
}
```

---

### Completion

Adds the total loaded amount to the `Total_Loaded` counter. For **Stockpile Manager** mode, releases the source resource that was seized before loading. Then exits via `Exit`.

```java
Total_Loaded += loaded;

if Load_Method == "Stockpile Manager"
{
  // Release source stockpile resource
  source = agent.properties[Stockpile_Manager_Node.ID].Source;
  agent.release_resource(source.Resource);
}

// Continue to next node
return Exit;
```
