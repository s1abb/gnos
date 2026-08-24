# Transship Destination Node

This node receives containers from a paired Transship Source node and loads them onto the current agent. It supports two modes controlled by the `Wait` input:

- **Wait** — blocks until the requested containers are available, then returns `Exit`.
- **No Wait** — attempts a non-blocking take of whatever containers are ready and returns `Incomplete`.

---

## Main Execution Logic

Behaviour branches on the `Wait` input. Both branches call into the `Source` object to acquire containers and append them to the agent's container list.

### Wait

Performs a blocking take from `Source`, suspending the agent until `Quantity` containers have been transferred (with an optional `Delay_Per_Container` between each). Returns `Exit` once all containers are loaded.

```java
if Wait {
  agent.agents += Source.Take(Quantity, Delay_Per_Container, agent);
  
  return Exit;
}
```

---

### No Wait

Performs a non-blocking attempt to take containers from `Source`. Loads any containers that are immediately available and returns `Incomplete` to signal that the transfer may not be fully satisfied.

```java
} else {
  agent.agents += Source.Attempt_Take(Quantity, Delay_Per_Container, agent);

  return Incomplete;
}