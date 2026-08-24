# Load New Agents Node

This node spawns `Count` new agents of `New_Agent_Type` and immediately loads them onto the current agent. Optionally, an `On_Spawned` callback is invoked for each new agent after it is created, allowing per-agent initialisation logic to be injected.

---

## Main Execution Logic

```java
index = 0;
// Spawn and load Count new agents into the current agent
while index < Count {
  // Spawn a new idle agent
  new_agent = simulation.spawn(New_Agent_Type);
  // Optionally invoke an initialisation hook for the new agent
  if On_Spawned != null {
    On_Spawned(new_agent, index);
  }
  // Load the new agent onto the carrier
  agent.agents.push(new_agent);
  index++;
}

return Exit;
```