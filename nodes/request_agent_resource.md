# Request Agent Resource Node

This node acquires a resource agent from an `Agent_Resource_Queue` on behalf of the entering agent. The resource agent is returned immediately (or dequeued when one becomes available), the entering agent is attached to it, the node's occupancy counters are adjusted manually, and the resource agent is jumped to its exit.

---

## Main Execution Logic

```java
// Block until a resource agent is available, then claim it
res_agent = Agent_Resource_Queue.request(agent);
res_agent.agents.push(agent);
// Manually decrement/increment counters because the agent exits via jump, bypassing normal node accounting
node.current--;
node.exited++;
res_agent.jump(exit);
```