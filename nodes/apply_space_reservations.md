# Apply Space Reservations Node

This node transfers yard space reservations held on the carrier agent onto its sub-agents' container properties, pairing them in order. Reservations are consumed from the front of the agent's `space_reservations` list and assigned one-by-one to each sub-agent until either all reservations or all sub-agents are exhausted.

---

## Main Execution Logic

```java
// Initialise the space_reservations list if not already present
if agent.properties.space_reservations == null {
  agent.properties.space_reservations = [];
}

// Assign each reservation to the next sub-agent's container in order
i = 0;
while agent.properties.space_reservations.len() > 0 and i < agent.agents.len() {
  agent.agents[i].properties.container.reservation = agent.properties.space_reservations.pop(0);
  i++;
}

return Exit;
```