# Agent Resource Queue Node

This node manages a pool of waiting locations and coordinates agents queuing for them. Each waiting location has a capacity-1 resource; agents compete to seize one before navigating to it.

When an agent enters this node it goes through four phases:

1. **Initialisation** — on first run, creates a resource for each entry in `Waiting_Locations` and stores them in the shared `location_list`.
2. **Queue loop** — repeatedly checks whether any waiting location is free. If another agent has already queued a hand-off request (via `requests`), properties are swapped and the requesting agent is unblocked immediately. Otherwise the agent seizes the first available location resource, or delays on `location_queue` until one becomes free.
3. **Navigation** — drives the agent's vehicle to the claimed waiting location.
4. **Handoff or park** — on arrival, if a new request is waiting the agent immediately passes its location to the requesting agent and returns. If no request is waiting, the agent pushes itself onto `agent_queue` to be claimed by a future request.

The node uses several shared node-level variables — `location_list`, `requests`, `agent_queue`, `location_queue`, and `id` — that persist across agent visits to coordinate the queue.

---

## Main Execution Logic

### Initialisation

Populates `location_list` on the first agent to enter this node. Each entry in `Waiting_Locations` gets a new capacity-1 resource paired with its location reference. Subsequent agents skip this block as `location_list` is already populated.

```java
if location_list.len() == 0 {
  for location in Waiting_Locations {
    location_list.push({
      "resource": simulation.create_resource(1),
      "location": location.Location
    });
  }
}
```

---

### Queue Loop

Loops until the agent has either been handed off via a pending request or has seized a free waiting location resource.

On each iteration, if `requests` is non-empty the agent at the front of the queue is granted access: the current agent's properties are copied to the requesting agent, the requesting agent's `id` property is set to reference the current agent as its parent, and the request event is triggered so the requesting agent can resume — the current agent then returns immediately without navigating.

If no requests are pending, the agent scans `location_list` for a location whose resource has remaining capacity. The first available one is seized and stored in `agent.properties[id]`. If none are free, the agent delays on the `location_queue` event until woken by a departing agent.

```java
while true {

  if requests.len() > 0 {
    first_request = requests.pop(0);
    // Copy current agent's properties to the requesting agent
    for key in first_request.agent.properties {
      agent.properties[key] = first_request.agent.properties[key];
    }
    // Set requesting agent's parent reference to this agent
    first_request.agent.properties[id] = {
      "parent": agent,
    };
    first_request.event.trigger();
    return;
  }
  found = false;
  for location in location_list {
    if location.resource.remaining > 0 {
      agent.seize_resource(location.resource);
      agent.properties[id] = location;
      found = true;
      break;
    }
  }
  if found {
    break;
  }
  // No location available — wait until a location is freed
  agent.delay(location_queue);
}
```

---

### Navigation

Drives the agent's vehicle to the claimed waiting location. The destination is set and the vehicle runs to it. If the destination cannot be set (e.g. the vehicle is already there), navigation is skipped.

```java
if (agent.vehicle.set_destination(agent.properties[id].location, [], [])) {
    agent.vehicle.run_to_destination();
}
```

---

### Handoff or Park

On arrival, checks once more whether a request has accumulated while the agent was travelling. If so, the agent releases its location resource, triggers `location_queue` to wake any agents waiting in the loop, copies properties to the requesting agent, sets the parent reference, triggers the request event, and returns — effectively passing the location slot on without the agent parking.

If no request is waiting, the agent pushes itself onto `agent_queue` to be claimed by the next incoming agent.

```java
if requests.len() > 0 {
    first_request = requests.pop(0);
    // Release location resource and wake the queue
    agent.release_resource(agent.properties[id].resource);
    location_queue.trigger();
    // Copy properties to requesting agent and unblock it
    for key in first_request.agent.properties {
      agent.properties[key] = first_request.agent.properties[key];
    }
    first_request.agent.properties[id] = {
      "parent": agent,
    };
    first_request.event.trigger();
    return;
}
// No pending request — park and wait to be claimed
agent_queue.push(agent);
```
