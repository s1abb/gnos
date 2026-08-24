# Load Container Node

This node fills an agent's container property with a tagged quantity of material. It initialises the container if one does not yet exist, rejects agents that are already carrying a load, then fills the container according to one of two load modes: **Default** (fixed delay or rate-based delay with a pre-configured material) or **Flowsim** (draws material from a connected `Source` and delays until the output container is full). Once loading completes the container is stamped with `Tag`, `Quantity`, and resolved material properties.

---

## Main Execution Logic

The node ensures the agent has a container record, validates it is empty, resolves material properties via the selected `Load_Type`, and then writes the final values onto the container.

### Initialise and Validate Container

If the agent has no container property yet, an empty container record is created. A non-null `tag` on an existing container means the agent is already loaded, which is an error condition.

```java
if agent.properties.container == null {
  agent.properties.container = {
    "tag": null,
    "quantity": 0,
    "material_properties": null,
    "reservation": null,
  };
}

// Prevent loading on top of an existing load
if agent.properties.container.tag != null {
  throw_error("Cannot load non-empty container");
}
```

---

### Default

Uses a statically configured `Material_Properties` value. The agent is delayed either by a fixed `Delay` duration or by a computed `Quantity / Rate` duration depending on `Delay_Type`.

```java
if Load_Type == "Default" {
  material_properties = Material_Properties;
  
  if Delay_Type == "Duration" {
    agent.delay(Delay);
  } else if Delay_Type == "Rate" {
    agent.delay(Quantity / Rate);
  } else {
    throw_error("Unknown delay type: " + stringify(Delay_Type));
  }
}
```

---

### Flowsim

Draws material from a connected `Source` by creating an output container for the requested `Quantity` at the given `Rate`. The agent delays until that container is full, then blended material properties are extracted and the temporary output container is cleaned up.

```java
} else if Load_Type == "Flowsim" {
  c = Source.create_output_container(0, Quantity, Rate);
  // Block until the source has filled the output container
  agent.delay(c.delay_until_full());
  material_properties = c.get_blended_material();
  c.remove();
} else {
  throw_error("Unknown load type: " + stringify(Load_Type));
}
```

---

### Stamp the Container

After the delay completes, the container is written with the resolved tag, quantity, and material properties.

```java
agent.properties.container["tag"] = Tag;
agent.properties.container["quantity"] = Quantity;
agent.properties.container["material_properties"] = material_properties;

return Exit;
```