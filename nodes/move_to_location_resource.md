# Move To Location Resource Node

This node drives an agent to the physical location associated with its allocated location resource, then acquires a final resource lock before exiting. It supports optional waypoints (`Passing_Locations`) and future destination hints (`Future_Locations`) to guide vehicle routing. The agent blocks at the end until the final resource is granted.

---

## Main Execution Logic

The destination is resolved from the agent's previously allocated resource (stored under `Location_Resource.id` in `agent.properties`). Passing and future locations are built into lists and passed to the vehicle's routing call. If the vehicle accepts the destination it is driven there immediately. Once arrived, the node requests the final resource and delays the agent until it is granted.

```java
// Resolve the physical location from the agent's allocated resource slot
Location = agent.properties[Location_Resource.id].item.location;

// Build optional ordered waypoints to pass through en route
passing = [];
if Passing_Locations {
    for loc in Passing_Locations {
        passing.push(loc.Location);
    }
}

// Build optional future destination hints for lookahead routing
future_locations = [];
if Future_Locations {
    future_locations = Future_Locations;
}

if (agent.vehicle.set_destination(Location, passing, future_locations)) {
    agent.vehicle.run_to_destination();
}

// Request the final resource lock and block until it is granted
agent.properties[Location_Resource.id].final_request = agent.properties[Location_Resource.id].item.final_resource.request();
agent.delay(agent.properties[Location_Resource.id].final_request);
return exit;
```