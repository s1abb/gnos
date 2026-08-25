if agent.properties.container == null {
  agent.properties.container = {
    "tag": null,
    "quantity": 0,
    "material_properties": null,
    "reservation": null,
  };
}

container = agent.properties.container;

quantity = Quantity;
if quantity == null {
  quantity = container.quantity;
}

container.quantity -= quantity;

if container.quantity <= 0 {
  agent.properties.container["tag"] = null;
  agent.properties.container["quantity"] = 0;
  agent.properties.container["material_properties"] = null;
}

if Unload_Type == "Default" {
  if Delay_Type == "Duration" {
    agent.delay(Delay);
  } else if Delay_Type == "Rate" {
    agent.delay(quantity / Rate);
  } else {
    throw_error("Unknown delay type: " + stringify(Delay_Type));
  }
} else if Unload_Type == "Flowsim" {
  c = null;
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