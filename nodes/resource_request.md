# Resource Request Node

This node seizes a unit of `Resource` on behalf of the entering agent, blocking until one becomes available, then exits.

---

## Main Execution Logic

```java
agent.seize_resource(Resource);
return exit;
```