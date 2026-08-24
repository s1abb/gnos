# Go To Location (Location Manager) Node

This node drives an agent to the physical location associated with its allocation in a `Location_Manager`. It supports optional waypoints (`Passing_Locations`) and future destination hints (`Future_Locations`) to guide vehicle routing. If the vehicle accepts the destination it is driven there and the node exits normally; if the destination cannot be set the node exits via `failed_exit`.

---

## Main Execution Logic

The destination is resolved from the agent's allocation record stored under `Location_Manager.ID` in `agent.properties`. Passing and future location lists are built from the optional inputs and passed to the vehicle's routing call.

```java
// Resolve the physical location from the agent's allocation in the location manager
Location = agent.properties[Location_Manager.ID].item.location;

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
    return exit;
} else {
    // Destination could not be set (e.g. no valid path); route to failed exit
    return failed_exit;
}
```