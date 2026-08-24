# Reserve Yard Container Tags Node

This node reserves containers in a `Container_Yard` using an explicit list of tags (rather than a single repeated tag), storing the reservations on the agent for a later take step. It supports two modes controlled by the `Blocking` input:

- **Blocking** — waits until all containers matching `Tags` are available, then stores the reservations and returns `Exit`.
- **Non-blocking** — attempts the reservation immediately; returns `Exit` on success or `Failed` if the containers are not available.

---

## Main Execution Logic

Before branching, the node ensures the agent has a `container_reservations` list to accumulate reservations across multiple calls.

```java
// Initialise the reservations list on the agent if not already present
if !agent.properties.container_reservations {
  agent.properties.container_reservations = [];
}
```

---

### Blocking

Reserves all containers matching the `Tags` list from the yard, blocking until they are all available. Appends the resulting reservations to the agent's list and returns `Exit`.

```java
if Blocking {
  agent.properties.container_reservations += Container_Yard.Reserve_Take(Tags);
}
```

---

### Non-blocking

Attempts a non-blocking reservation for all containers in `Tags`. If successful, appends the reservations and returns `Exit`; otherwise returns `Failed` without modifying the agent's reservation list.

```java
} else {
  reservations = Container_Yard.Attempt_Reserve_Take(Tags);
  if reservations != null {
    agent.properties.container_reservations += reservations;
    return Exit;
  } else {
    return Failed;
  }
}

return Exit;
```