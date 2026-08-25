index = 0;
// Spawn and load "Count" number of new agents into the current agent
while index < Count {
  // Spawn a new idle agent
  new_agent = simulation.spawn(New_Agent_Type);
  // Trigger optional initialisation hook for new agent
  if On_Spawned != null {
    On_Spawned(new_agent, index);
  }
  // Load new agent into current agent
  agent.agents.push(new_agent);
  index++;
}

// Continue to next node
return Exit;