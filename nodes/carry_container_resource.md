# Carry Container Resource Node

This node retrieves a specific container from a `Container_Yard` and loads it onto the agent, gated by a resource lock. It reserves the container by the agent's tag, acquires a `Resource`, performs the take, holds the resource for a configurable `Delay`, then releases it before exiting.

---

## Main Execution Logic

The node runs as a single sequential block: reserve, acquire resource, take container, delay, release.

```java
// Reserve the container matching this agent's tag
reservation = Container_Yard.Reserve_Take([agent.tag])[0];

// Acquire the resource lock before performing the physical take
r = Resource.request();
agent.delay(r);

// Take the reserved container and add it to the agent's container list
agent.agents.push(Container_Yard.Take({"reservation": reservation}));

// Hold the resource for the configured delay (e.g. crane operation time)
agent.delay(Delay);

// Release the resource so other agents can use it
r.release();

return Exit;
```