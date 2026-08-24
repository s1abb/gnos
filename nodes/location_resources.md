# Location Resources Node

This node runs once at simulation start to initialise the shared `resource_list` used by other nodes in the location management system. For each entry in `Locations` it creates two resources — a queue resource sized by `Queue Size` and a capacity-1 final resource — paired with the location reference. The node terminates immediately after setup.

---

## Main Execution Logic

Iterates over every entry in `Locations`, creates a queue resource and a final resource for each, and pushes the combined item onto `resource_list`. Terminates the node once all entries are processed.

```java
for location in Locations {
  item = {
    // Queue resource sized by the location's configured Queue Size
    "resource": simulation.create_resource(location["Queue Size"]),
    // Final resource with capacity 1 — controls access to the location itself
    "final_resource": simulation.create_resource(1),
    "location": location.Location
  };
  resource_list.push(item);
}

// Setup complete — terminate this node
node.terminate();
```
