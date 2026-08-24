# Request Location Resource Node

This node submits a resource request on behalf of the entering agent to a connected `Location_Resource`. The agent is passed directly to the resource's `request` method, which handles queuing and allocation internally, and the node exits immediately.

---

## Main Execution Logic

```java
Location_Resource.request(agent);
return exit;
```