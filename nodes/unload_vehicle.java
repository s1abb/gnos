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

Total_Unloaded += unloaded;

// Continue to next node
return Exit;