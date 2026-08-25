if agent.properties.container == null {
  agent.properties.container = {
    "tag": null,
    "quantity": 0,
    "material_properties": null,
    "reservation": null,
  };
}

if agent.properties.container.tag != null {
  throw_error("Cannot load non-empty container");
}

material_properties = null;

if Load_Type == "Default" {
  material_properties = Material_Properties;
  
  if Delay_Type == "Duration" {
    agent.delay(Delay);
  } else if Delay_Type == "Rate" {
    agent.delay(Quantity / Rate);
  } else {
    throw_error("Unknown delay type: " + stringify(Delay_Type));
  }
} else if Load_Type == "Flowsim" {
  c = Source.create_output_container(0, Quantity, Rate);
  agent.delay(c.delay_until_full());
  material_properties = c.get_blended_material();
  c.remove();
} else {
  throw_error("Unknown load type: " + stringify(Load_Type));
}

agent.properties.container["tag"] = Tag;
agent.properties.container["quantity"] = Quantity;
agent.properties.container["material_properties"] = material_properties;

return Exit;