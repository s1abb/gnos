# Transship Source Node

This node transfers containers from the current agent to one or more paired Transship Destination nodes. It first collects the containers to send, immediately satisfies any waiting destinations, then branches on the `Wait` input for any remainder:

- **All consumed** — every container was claimed by a waiting destination; the node delays on all release events and returns `Exit`.
- **No Wait** — unmatched containers are returned to the agent and the node returns `Incomplete`.
- **Wait** — the node registers itself as a source for any remaining containers so future destinations can claim them, delays on all release events, and returns `Exit`.

---

## Main Execution Logic

The node begins by collecting containers from the agent, then immediately distributes them to any already-waiting destinations. The final branch depends on whether all containers were consumed and on the `Wait` input.

### Collect Containers

If `Quantity` is null, all containers are taken from the agent. Otherwise, up to `Quantity` containers are popped from the front of the agent's list (capped to however many are actually available).

```java
containers = [];

if Quantity == null {
  // Take all containers from the agent
  containers = agent.agents;
  agent.agents = [];
} else {
  // Cap Quantity to the number of containers actually available
  if Quantity > agent.agents.len() {
    Quantity = agent.agents.len();
  }
  i = 0;
  while i < Quantity {
    containers.push(agent.agents.pop(0));
    i++;
  }
}
```

---

### Distribute to Waiting Destinations

Each entry in `Destinations` represents a destination node that is already waiting for containers. The loop pairs containers with destinations one-by-one, creating a release event for each pair. Once a destination has received all the containers it desires, it is removed from the front of the list.

```java
events = [];
while Destinations.len() > 0 and containers.len() > 0 {
  event = simulation.create_event();
  events.push(event);
  // Push the container and its release callback to the destination
  Destinations[0].containers.push({
    "container": containers.pop(0),
    "release": func() {
      // Guard against double-open
      if event.is_open {
        throw_error("opened already");
      }
      event.open();
    },
  });
  
  dest = Destinations[0];
  // Remove destination from the queue once it has enough containers
  if dest.desired <= dest.containers.len() {
    Destinations.pop(0);
  }
  dest.release();
}
```

---

### All Containers Consumed

If every collected container was matched to a waiting destination, the node delays on all release events (waiting for destinations to confirm receipt) and exits normally.

```java
if containers.len() == 0 {
  for event in events {
    agent.delay(event);
  }
  return Exit;
}
```

---

### No Wait

If there are still unmatched containers and `Wait` is false, the node delays on events already created, returns the leftover containers to the agent, and signals an incomplete transfer.

```java
if !Wait {
  for event in events {
    agent.delay(event);
  }
  agent.agents += containers;
  return Incomplete;
}
```

---

### Wait

If `Wait` is true and containers remain, the node packages them into a `source` object (each with its own release event) and pushes it onto the `Sources` list so that future Transship Destination nodes can claim them. It then delays on all events and returns `Exit` once all containers have been released.

```java
source = {"containers": []};
while containers.len() > 0 {
  event = simulation.create_event();
  events.push(event);
  // Register each remaining container with a release callback
  source.containers.push({
    "container": containers.pop(0),
    "release": func() {
      // Guard against double-open
      if event.is_open {
        throw_error("opened already");
      }
      event.open();
    },
  });
}
// Make this source available for waiting destinations
Sources.push(source);

for event in events {
  agent.delay(event);
}

return Exit;