# Unload Agent Node

This node unloads all cargo agents currently carried by the agent's vehicle. Each carried agent is jumped to the `Cargo` node and the vehicle's agent list is cleared. The node then exits via `Main`.

---

## Main Execution Logic

Iterates over every agent in `agent.agents`, redirects each one to the `Cargo` output, and increments the node's exit counter. After all cargo has been unloaded the carried agents list is cleared and the node exits via `Main`.

```java
for cargo in agent.agents {
  // Route each cargo agent to the Cargo output and count the exit
  node.exited++;
  cargo.jump(Cargo);
}

// Clear the vehicle's carried agents list
agent.agents = [];

return Main;
```
