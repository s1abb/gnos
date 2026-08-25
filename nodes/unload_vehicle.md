# Unload Vehicle Node

This node unloads material from the components of an agent's vehicle. It first selects which components to unload using `Selection_Method`, then determines how much to unload per component using `Unload_Method`. Unloading simulates the time taken to transfer material, optionally depositing into a flow simulation container.

Component selection supports three methods:

- **Type** — selects components matching a specified `Component_Type`.
- **Index** — selects a slice of components between `Start_Index` and `End_Index`.
- **All** — selects every component on the vehicle.

Unload amount supports four methods:

- **Unload Per Component** — unloads a fixed amount from each component.
- **Shared Amount** — divides `Max_Shared_Amount` evenly across all selected components.
- **Fully Unload** — empties each component of its full current load.
- **Target Amount** — unloads up to a total `Target_Amount` across all components, stopping early once reached.

The node exits immediately via `Exit` if no components are selected or if the calculated unload amount per component is zero. On completion it increments `Total_Unloaded` and exits via `Exit`.

---

## Main Execution Logic

### Component Selection

Builds the list of vehicle components to unload based on `Selection_Method`. Exits immediately if no components are found.

```java
// Get components
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

### Unload Amount Calculation

Determines the maximum amount to unload per component from `Unload_Method`. Only **Unload Per Component** and **Shared Amount** compute this value up front; **Fully Unload** and **Target Amount** are resolved per component inside the unloading loop. Exits immediately if the calculated unload amount per component is zero.

```java
// Determine maximum load per component
max_load_per_component = null;
if Unload_Method == "Unload Per Component"
{
  max_load_per_component = Unload_Per_Component;
}
else if Unload_Method == "Shared Amount"
{
  max_load_per_component = Max_Shared_Amount / components.len();
}

// Nothing to load, continue to next node
if max_load_per_component == 0
{
  return Exit;
}
```

---

### Unloading Loop

Iterates over each selected component, calculates the unload amount for that component, simulates the transfer duration, and removes the load. Components with no current load are skipped. For **Target Amount** mode, unloading stops as soon as the cumulative unloaded amount reaches `Target_Amount`.

The transfer duration is determined by `Unload_Rate` unless `Use_Unload_Duration` is set, in which case the rate is derived from `Unload_Component_Duration`. If a `Destination_Container` is configured, material is deposited into a flow simulation container (optionally tagged with `Material`); otherwise the delay is simulated directly.

```java
// Track total unloaded amount
unloaded = 0;

// Loop over selected components
for component in components {
  // Calculate unload amount
  unload_amount = max_load_per_component;
  current_load = max([component.current_load, 0]);
  if Unload_Method == "Fully Unload" {
    unload_amount = current_load;
  }
  else if Unload_Method == "Target Amount"
  {
    // Ensure we do not unload more than target amount
    if unloaded + current_load > Target_Amount
    {
      unload_amount = Target_Amount - unloaded;
    }
    else
    {
      unload_amount = current_load;
    }
  }
  else if unload_amount > current_load
  {
    unload_amount = current_load;
  }

  // Nothing to load, continue to next component
  if current_load == 0
  {
    continue;
  }

  // Calculate unload rate
  rate = Unload_Rate;
  if Use_Unload_Duration
  {
    rate = unload_amount / Unload_Component_Duration;
  }

  if Destination_Container
  {
    // Transfer to flow sim container
    c = null;
    if Material {
        c = Destination_Container.create_input_container(current_load, unload_amount, rate, Material);
    } else {
        c = Destination_Container.create_input_container(current_load, unload_amount, rate);
    }
    agent.delay(c.delay_until_empty());
    c.remove();
  }
  else
  {
    // Simulate time it would take to transfer material
    agent.delay(unload_amount / rate);
  }

  // Remove load from component
  component.add_load(-unload_amount);

  // Sum unloaded amount
  unloaded += unload_amount;

  // If reached target amount stop unloading
  if Unload_Method == "Target Amount" and unloaded >= Target_Amount
  {
    break;
  }
}
```

---

### Completion

Adds the total unloaded amount to the `Total_Unloaded` counter, then exits via `Exit`.

```java
Total_Unloaded += unloaded;

// Continue to next node
return Exit;
```