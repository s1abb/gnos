# Release Containers Node

This node removes containers from a carrier agent's `agents` list and releases them according to one of three modes:

- **Output Agents** — pops containers and forwards each to a connected `Container` output node.
- **Yard Request** — pops containers and stores each in a `Container_Yard`.
- **Yard Reserved** — stores only containers whose reservation targets `Container_Yard`, either all matching containers or just the first, leaving the rest on the agent.

In all modes `Quantity` controls how many containers are released; if `Quantity` is null the agent's full load is used. An optional `Delay_Per_Container` is applied between each release.

---

## Main Execution Logic

The node branches on `Release_Type`. An unknown value throws an error. All branches exit via `return Carrier`.

### Output Agents

Pops up to `Quantity` containers (defaulting to the full `agents` list) and routes each to the `Container` exit, with an optional per-container delay. The loop breaks early if the agent runs out of containers before reaching the quantity.

```java
if Release_Type == "Output Agents" {
  quantity = Quantity;
  // Default to releasing all carried containers if no quantity is specified
  if quantity == null {
    quantity = agent.agents.len();
  }
  
  i = 0;
  while i < quantity {
    if agent.agents.len() == 0 {
      break;
    }
    a = agent.agents.pop(0);
    if Delay_Per_Container > 0 {
      agent.delay(Delay_Per_Container);
    }
    a.set_next(Container);
    i++;
  }
}
```

---

### Yard Request

Pops up to `Quantity` containers (defaulting to the full `agents` list) and stores each in `Container_Yard`, with an optional per-container delay. The loop breaks early if the agent runs out of containers.

```java
} else if Release_Type == "Yard Request" {
  quantity = Quantity;
  // Default to releasing all carried containers if no quantity is specified
  if quantity == null {
    quantity = agent.agents.len();
  }
  
  i = 0;
  while i < quantity {
    if agent.agents.len() == 0 {
      break;
    }
    a = agent.agents.pop(0);
    if Delay_Per_Container > 0 {
      agent.delay(Delay_Per_Container);
    }
    Container_Yard.Store(a);
    i++;
  }
}
```

---

### Yard Reserved

Walks the agent's `agents` list and stores containers whose `container.reservation` targets `Container_Yard`; containers with no reservation or a different yard are kept. If `All` is set every matching container is stored; otherwise only the first match is stored and the loop breaks. The agent's `agents` list is replaced with the filtered result.

```java
} else if Release_Type == "Yard Reserved" {
  i = 0;
  newList = [];
  while i < agent.agents.len() {
    ca = agent.agents[i];
    // Keep containers with no reservation or a reservation for a different yard
    if !ca.properties.container.reservation or ca.properties.container.reservation.container_yard != Container_Yard {
      newList.push(ca);
    } else {
      if Delay_Per_Container > 0 {
        agent.delay(Delay_Per_Container);
      }
      if All {
        // Store all matching reserved containers
        Container_Yard.Store(ca);
      } else {
        // Store only the first match then stop; keep remaining entries
        newList = agent.agents.slice(i, 1);
        Container_Yard.Store(ca);
        break;
      }
    }
    i++;
  }
  // Replace the agents list with only the containers that were not released
  agent.agents = newList;
} else {
  throw_error("unknown release type: " + stringify(Release_Type));
}

return Carrier;
```