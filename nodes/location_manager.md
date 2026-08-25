# Location Manager Node

This node initialises a pool of manageable locations at simulation start. For each entry in `Locations` it builds a checkout record used to track whether that location is currently occupied, and creates a shared `Resource` sized to the number of locations so that callers elsewhere can seize and release individual locations.

---

## Main Execution Logic

The node runs once at startup, building the `Location_Checkout` list and the corresponding `Resource`.

```java
for location in Locations {
  Location_Checkout.push({
    "location": location.Location,
    "in_use": false
  });
}

Resource = simulation.create_resource(Locations.len());
```