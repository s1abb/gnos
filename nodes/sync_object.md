# Sync Object Node

This node synchronises a property on the agent's bound object to an external resource, weighted by a specified factor. The `Bound_Object_Type` input selects whether the target is a **Calculation Node** or a piece of **Equipment**.

- **Calculation Node** — binds the object's property to a calculation node retrieved by `Calculation_Node_Name`.
- **Equipment** — binds the object's property to an equipment resource retrieved by `Equipment_Name`.

---

## Main Execution Logic

Branching is driven by `Bound_Object_Type`. Each branch retrieves the target resource from the simulation, then calls `sync` on the agent's object with the specified `Sync_Property` and `Weight`.

### Calculation Node

Fetches the named calculation node and synchronises the object property to it.

```java
if Bound_Object_Type == "Calculation Node" {
  calc_node = simulation.get_calculation_node(Calculation_Node_Name);
  agent.object.sync(Sync_Property, calc_node, Weight);
}
```

---

### Equipment

Fetches the named equipment resource and synchronises the object property to it.

```java
else if Bound_Object_Type == "Equipment" {
  equipment = simulation.get_equipment(Equipment_Name);
  agent.object.sync(Sync_Property, equipment, Weight);
}
```

---

## Termination & Loop Control

```java
return Exit;
```