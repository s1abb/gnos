# Carry Containers Node

This node loads containers onto a carrier agent according to one of three carry modes:

- **Yard Request** — takes a fixed `Quantity` of containers from a `Container_Yard`, optionally honouring pre-existing reservations, with an optional per-container delay.
- **Yard Reserved** — collects containers from a `Container_Yard` that were previously reserved, either all matching reservations or just one, with an optional per-container delay.
- **Input Agents** — uses a semaphore `Resource` to synchronise a carrier agent with individual container agents arriving at the same node; the carrier waits for each container agent in turn while containers register themselves in a shared queue.

---

## Main Execution Logic

The node branches entirely on `Carry_Type`. An unknown value throws an error.

### Yard Request

Initialises `container_reservations` on the agent if not already present, then collects any existing reservations that match both `Tag` and `Container_Yard`. For each unit of `Quantity`, the node builds a request — using a matching reservation if one exists, otherwise a plain tag request — calls `Container_Yard.Take()`, pushes the returned container onto the agent, and optionally delays by `Delay_Per_Container`.

```java
if Carry_Type == "Yard Request" {
  if !agent.properties.container_reservations {
    agent.properties.container_reservations = [];
  }
  
  // Collect any pre-existing reservations that match this tag and yard
  reservations = [];
  i = 0;
  for reservation in agent.properties.container_reservations {
    if reservation.tag == Tag and reservation.container_yard == Container_Yard {
      reservations.push(reservation);
    }
  }

  i = 0;
  while i < Quantity {
    request = {"tag": Tag};
    // Prefer a reservation if one is available; otherwise request by tag only
    if reservations.len() > 0 {
      request = {"reservation": reservations.pop(0)};
      agent.properties.container_reservations.remove(request.reservation);
    }
    agent.agents.push(Container_Yard.Take(request));
    if Delay_Per_Container > 0 {
      agent.delay(Delay_Per_Container);
    }
    i++;
  }
}
```

---

### Yard Reserved

Initialises `container_reservations` if needed, then walks the list and collects containers whose reservation targets `Container_Yard`. Non-matching reservations are kept in a new list. If `All` is set, every matching reservation is fulfilled; otherwise only the first is taken and the loop breaks. The reservations list is replaced with the filtered result.

```java
} else if Carry_Type == "Yard Reserved" {
  if !agent.properties.container_reservations {
    agent.properties.container_reservations = [];
  }
  
  i = 0;
  newList = [];
  while i < agent.properties.container_reservations.len() {
    r = agent.properties.container_reservations[i];
    if r.container_yard != Container_Yard {
      // Keep reservations that belong to a different yard
      newList.push(r);
    } else {
      if All {
        // Collect all matching reserved containers
        agent.agents.push(Container_Yard.Take({"reservation": r}));
        if Delay_Per_Container > 0 {
          agent.delay(Delay_Per_Container);
        }
      } else {
        // Collect only the first matching reservation then stop
        newList = agent.properties.container_reservations.slice(i, 1);
        agent.agents.push(Container_Yard.Take({"reservation": r}));
        if Delay_Per_Container > 0 {
          agent.delay(Delay_Per_Container);
        }
        break;
      }
    }
    i++;
  }
  // Replace the reservations list with only the unclaimed entries
  agent.properties.container_reservations = newList;
}
```

---

### Input Agents

Two agent types share this branch. Container agents register themselves in the `Containers` queue and expand `Resource.capacity` by one, then hold. Carrier agents issue `Quantity` resource requests up front (in parallel), then collect each granted request in order: delay until the request is fulfilled, pop the next container from the queue, push it onto the carrier, optionally delay by `Delay_Per_Container`, shrink capacity back down, release the resource slot, and decrement `node.current`. The carrier exits explicitly; container agents return `null` to remain in the node until claimed.

```java
} else if Carry_Type == "Input Agents" {
  if agent.agent_type == Carrier_Agent_Type {
    if Quantity == null {
      throw_error("Carrier agent must have Quantity parameter set");
    }
    i = 0;
    // Issue all resource requests before waiting, so containers can be claimed concurrently
    requests = [];
    while i < Quantity {
      requests.push(Resource.request());
      i++;
    }
    
    for r in requests {
      agent.delay(r);
      ca = Containers.pop(0);
      if ca == null {
        throw_error("failed to get agent");
      }
      agent.agents.push(ca);
      if Delay_Per_Container > 0 {
        agent.delay(Delay_Per_Container);
      }
      // Shrink capacity and release the slot now that this container has been claimed
      Resource.capacity -= 1;
      Resource.release(r);
      node.current--;
    }
  
    return Exit;
  } else {
    // Container agent: register in the queue and signal that a slot is available
    Containers.push(agent);
    Resource.capacity += 1;
    return null;
  }
} else {
  throw_error("unknown carry type: " + stringify(Carry_Type));
}
return Exit;
```