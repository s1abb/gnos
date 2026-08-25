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

  // If reached target amount stop unloading and continue to next node
  if Load_Method == "Target Amount" and loaded >= target_load
  {
    break;
  }
}

Total_Loaded += loaded;

if Load_Method == "Stockpile Manager"
{
  // Release source stockpile resource
  source = agent.properties[Stockpile_Manager_Node.ID].Source;
  agent.release_resource(source.Resource);
}

// Continue to next node
return Exit;