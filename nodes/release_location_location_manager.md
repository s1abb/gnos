# Release Location (Location Manager) Node

This node releases an agent's previously allocated location back to the `Location_Manager`. It retrieves the original resource request from the agent's allocation record, marks the location as no longer in use, and releases the resource so the manager can assign it to another agent.

---

## Main Execution Logic

```java
// Retrieve the resource request handle stored when the location was allocated
request = agent.properties[Location_Manager.ID].request;
// Mark the location item as available for future allocations
agent.properties[Location_Manager.ID].item.in_use = false;

Location_Manager.Resource.release(request);

return exit;
```