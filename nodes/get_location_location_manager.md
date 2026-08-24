# Get Location (Location Manager Node)

This node claims an available location from a `Location_Manager` for the current agent. It first requests the manager's resource — blocking until one becomes free — then finds the first unclaimed entry in the manager's `Location_Checkout` list and marks it as in use. The claimed location and the resource request handle are stored on the agent's properties so downstream nodes (e.g. unload or release nodes) can reference them.

If no available location is found after the resource is granted, the node throws an error. On success it exits via `exit`.

---

## Main Execution Logic

### Resource Request

Requests the `Location_Manager`'s resource and blocks the agent until it is granted. This ensures only as many agents proceed as there are available locations.

```java
request = Location_Manager.Resource.request();

agent.delay(request);
```

---

### Location Claim

Searches `Location_Manager.Location_Checkout` for the first entry that is not currently in use. Throws an error if the resource was granted but no free location entry is found (which indicates a misconfiguration between resource capacity and checkout list size). Marks the claimed entry as in use and stores both the location item and the request handle on the agent for downstream use.

```java
location_found = false;

for item in Location_Manager.Location_Checkout {
  if item["in_use"] == false {
    location_found = item;
    break;
  }
}

if location_found == false {
  throw_error("Failed to claim location from Location Manager:", Location_Manager.name);
}

location_found["in_use"] = true;
// Store the claimed location and request handle on the agent for downstream nodes
agent.properties[Location_Manager.ID] = {
  "item": location_found,
  "request": request
};
```

---

## Exit

```java
return exit;
```
