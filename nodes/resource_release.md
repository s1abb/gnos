# Resource Release Node

This node releases a previously seized unit of `Resource` held by the entering agent, freeing it for other agents waiting to seize it, then exits.

---

## Main Execution Logic

```java
agent.release_resource(Resource);
return exit;
```