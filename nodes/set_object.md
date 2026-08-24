# Set Object Node

This node retrieves a named object from the simulation space and assigns it to the current agent. It takes a single input, `Object_Name`, which specifies the name of the object to look up.

---

## Main Execution Logic

The node performs a direct lookup and assignment with no branching.

```java
// Retrieve the named object from the simulation space and assign it to the agent
object = Sim_Space.get_object(Object_Name);
agent.set_object(object);

return Exit;
```