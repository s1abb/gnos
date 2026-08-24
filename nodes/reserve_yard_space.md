# Reserve Yard Space Node

This node reserves storage space in a `Container_Yard` and attaches the resulting reservation(s) to the agent's container property so a later store step can use them. It supports two reserve types and, within each, a blocking or non-blocking attempt:

- **Carrier** — reserves space for multiple containers carried by sub-agents (`agent.agents`), attaching one reservation to each sub-agent's container property.
- **Container** — reserves a single space for the agent's own container property.

In both types, `Blocking` controls whether the node waits until space is available (`Exit`) or returns `Failed` immediately if space cannot be reserved.

---

## Main Execution Logic

Branching is driven by `Reserve_Type`. Each branch has its own inner blocking/non-blocking split.

### Carrier

Resolves the quantity to reserve (defaulting to the number of sub-agents if `Quantity` is null or exceeds the available count), performs the reservation, then assigns each reservation to the corresponding sub-agent's container property. Throws an error if any sub-agent already holds a reservation.

```java
if Reserve_Type == "Carrier" {
  reservations = [];
  
  // Default to the number of sub-agents if Quantity is unset or too large
  quantity = Quantity;
  if quantity == null or quantity > agent.agents.len() {
    quantity = agent.agents.len();
  }
  
  reservations = null;
  if Blocking {
    reservations = Container_Yard.Reserve_Store(quantity);
  } else {
    reservations = Container_Yard.Attempt_Reserve_Store(quantity);
    // Return Failed immediately if space is unavailable
    if reservations == null {
      return Failed;
    }
  }
  
  // Attach each reservation to the matching sub-agent's container
  i = 0;
  while i < reservations.len() {
    if agent.agents[i].properties.container.reservation != null {
      throw_error("Container has already reserved space");
    }
    agent.agents[i].properties.container.reservation = reservations[i];
    i++;
  }
}
```

---

### Container

Reserves a single space for the agent's own container. Throws an error if the agent's container already holds a reservation, then attaches the new reservation.

```java
} else if Reserve_Type == "Container" {
  if agent.properties.container.reservation != null {
    throw_error("Container has already reserved space");
  }
  
  reservations = null;
  if Blocking {
    reservations = Container_Yard.Reserve_Store(1);
  } else {
    reservations = Container_Yard.Attempt_Reserve_Store(1);
    // Return Failed immediately if space is unavailable
    if reservations == null {
      return Failed;
    }
  }
  agent.properties.container.reservation = reservations[0];
} else {
  throw_error("unknown reserve type: " + stringify(Reserve_Type));
}

return Exit;
```