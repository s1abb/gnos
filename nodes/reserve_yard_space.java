if Reserve_Type == "Carrier" {
  reservations = [];
  
  quantity = Quantity;
  if quantity == null or quantity > agent.agents.len() {
    quantity = agent.agents.len();
  }
  
  reservations = null;
  if Blocking {
    reservations = Container_Yard.Reserve_Store(quantity);
  } else {
    reservations = Container_Yard.Attempt_Reserve_Store(quantity);
    if reservations == null {
      return Failed;
    }
  }
  
  i = 0;
  while i < reservations.len() {
    if agent.agents[i].properties.container.reservation != null {
      throw_error("Container has already reserved space");
    }
    agent.agents[i].properties.container.reservation = reservations[i];
    i++;
  }
} else if Reserve_Type == "Container" {
  if agent.properties.container.reservation != null {
    throw_error("Container has already reserved space");
  }
  
  reservations = null;
  if Blocking {
    reservations = Container_Yard.Reserve_Store(1);
  } else {
    reservations = Container_Yard.Attempt_Reserve_Store(1);
    if reservations == null {
      return Failed;
    }
  }
  agent.properties.container.reservation = reservations[0];
} else {
  throw_error("unknown reserve type: " + stringify(Reserve_Type));
}

return Exit;