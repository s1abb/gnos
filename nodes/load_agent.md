# Load Agent Node

This node collects a set of agents into a single carrier agent. It supports two agent roles: a **Collected Agent** (an item to be picked up) and a **Collector Agent** (the carrier that gathers items up to its `quantity` limit). Collected agents queue until a collector arrives; collectors drain the queue immediately. Optionally, if `Call_Collector` is enabled and no collector is waiting, the node can pull one directly from a `Collector_Storage_Node`.

---

## Helper: `addToCollector`

Adds a collected agent to a collector's `agents` list. If the collector has now reached its required quantity, it is removed from `Collector_Queue` and, provided the current agent is not the collector itself, the node stats are updated and the collector is jumped to the exit.

```java
func addToCollector(collector, collected) {
  collector.agents.push(collected);
  if (collector.quantity <= (collector.agents.len())) {
      Collector_Queue.pop(0);
      // Only jump the collector to exit if a collected agent triggered this,
      // not when the collector itself just arrived and self-completed
      if (agent != collector) {
        node.exited += collector.agents.len();
        node.current -= collector.agents.len();
        node.current -= 1;
        collector.jump(exit);
      }
  }
}
```

---

## Main Execution Logic

The node branches on `agent.type`, routing collected items and collectors through separate paths, then falls through to `return null` to hold any agent that has not yet met its exit condition.

### Collected Agent

If a collector is already waiting in `Collector_Queue`, the collected agent is immediately handed off via `addToCollector`. Otherwise it is pushed to `Collected_Queue` to wait.

When `Call_Collector` is enabled and no collector was waiting, the node tries to pull one from `Collector_Storage_Node`. If a collector is available it is moved to `Collector_Queue` and the node drains as much of `Collected_Queue` as possible. If no collector is available, the node registers itself on `Collector_Storage_Node.Requests` so that the storage node can push a collector here later.

```java
if (agent.type == Collected_Agents_Type) {
  if (Collector_Queue.len() > 0) {
    c = Collector_Queue[0];
    addToCollector(c, agent);
  } else {
    Collected_Queue.push(agent);
    if Call_Collector {
      if Collector_Storage_Node.Collectors.len() > 0 {
        // Pull one collector from storage and dispatch it here
        Collector_Storage_Node.current -= 1;
        Collector_Storage_Node.exited += 1;
        collector = Collector_Storage_Node.Collectors.pop(0);
        Collector_Queue.push(collector);
        while ((Collected_Queue.len() > 0) and (Collector_Queue.len() > 0)) {
            c = Collector_Queue[0];
            collected = Collected_Queue.pop(0);
            addToCollector(c, collected);
        }
      }
      else {
        // No collector available; register a callback request on the storage node
        Collector_Storage_Node.Requests.push(node);
      }
    }
  }
}
```

---

### Collector Agent

The collector must have a `quantity` property; if it is missing an error is thrown. The collector is added to `Collector_Queue` and the node immediately tries to pair it with any agents already waiting in `Collected_Queue`.

If the collector reaches its quantity during this drain — or if `Depart_Immediately` is set — it is popped from the queue, node stats are updated, and it exits. Otherwise it remains in `Collector_Queue` and waits for more collected agents to arrive.

```java
if (agent.type == Collector_Type) {
  if agent.quantity == null {
    throw_error("Agent of type '" + agent.type + "' requires a 'quantity' parameter to work as a collector in the 'Load Agent' node.");
  }

  Collector_Queue.push(agent);

  // Drain collected agents that were already waiting
  while ((Collected_Queue.len() > 0) and (Collector_Queue.len() > 0)) {
    c = Collector_Queue[0];
    collected = Collected_Queue.pop(0);
    addToCollector(c, collected);
  }

  if (agent.quantity <= (agent.agents.len()) or Depart_Immediately) {
    Collector_Queue.pop(0);
    node.exited += agent.agents.len();
    node.current -= agent.agents.len();
    return exit;
  }
}
```

---

## Termination & Loop Control

Agents that have not yet met their exit condition (collected agents with no collector, or collectors still below capacity) are held in the node.

```java
return null;
```