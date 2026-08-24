# Unload Container Node

This node unloads material from the agent's container property into a destination, decrementing the tracked quantity and clearing the container state when empty. It supports two unload types:

- **Default** — simulates the unload as a simple time delay, either a fixed `Duration` or a quantity-divided-by-`Rate` calculation.
- **Flowsim** — creates a Flowsim input container at the `Destination` and delays until it drains, optionally passing through material properties.

---

## Main Execution Logic

The node begins by ensuring the agent has a `container` property, then resolves the quantity to unload and decrements the tracked amount.

### Initialise and Resolve Quantity

If the agent has no container property yet, it is initialised with empty defaults. The quantity to unload defaults to the full tracked amount if `Quantity` is not specified. Once resolved, the quantity is subtracted from the container and the container state is cleared if it reaches zero.

```java
// Initialise container property if not already present
if agent.properties.container == null {
  agent.properties.container = {
    "tag": null,
    "quantity": 0,
    "material_properties": null,
    "reservation": null,
  };
}

container = agent.properties.container;

// Default to unloading the full remaining quantity
quantity = Quantity;
if quantity == null {
  quantity = container.quantity;
}

// Decrement the tracked quantity
container.quantity -= quantity;

// Clear container state once fully unloaded
if container.quantity <= 0 {
  agent.properties.container["tag"] = null;
  agent.properties.container["quantity"] = 0;
  agent.properties.container["material_properties"] = null;
}
```

---

### Default

Delays for the duration of the unload using either a fixed `Delay` duration or a rate-based calculation (`quantity / Rate`). Throws an error if `Delay_Type` is unrecognised.

```java
if Unload_Type == "Default" {
  if Delay_Type == "Duration" {
    agent.delay(Delay);
  } else if Delay_Type == "Rate" {
    agent.delay(quantity / Rate);
  } else {
    throw_error("Unknown delay type: " + stringify(Delay_Type));
  }
}
```

---

### Flowsim

Creates a Flowsim input container at `Destination` with the given quantity and `Rate`, optionally forwarding `material_properties` if the container carries them. The node delays until the Flowsim container is empty, then removes it.

```java
} else if Unload_Type == "Flowsim" {
  c = null;
  // Pass material properties through if present
  if container.material_properties {
    c = Destination.create_input_container(quantity, quantity, Rate, container.material_properties);
  } else {
    c = Destination.create_input_container(quantity, quantity, Rate);
  }
  agent.delay(c.delay_until_empty());
  c.remove();
} else {
  throw_error("Unknown unload type: " + stringify(Unload_Type));
}

return Exit;
```