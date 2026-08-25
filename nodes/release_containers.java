if Release_Type == "Output Agents" {
  quantity = Quantity;
  if quantity == null {
    quantity = agent.agents.len();
  }
  
  i = 0;
  while i < quantity {
    if agent.agents.len() == 0 {
      break;
    }
    a = agent.agents.pop(0);
    if Delay_Per_Container > 0 {
      agent.delay(Delay_Per_Container);
    }
    a.set_next(Container);
    i++;
  }
} else if Release_Type == "Yard Request" {
  quantity = Quantity;
  if quantity == null {
    quantity = agent.agents.len();
  }
  
  i = 0;
  while i < quantity {
    if agent.agents.len() == 0 {
      break;
    }
    a = agent.agents.pop(0);
    if Delay_Per_Container > 0 {
      agent.delay(Delay_Per_Container);
    }
    Container_Yard.Store(a);
    i++;
  }
} else if Release_Type == "Yard Reserved" {
  i = 0;
  newList = [];
  while i < agent.agents.len() {
    ca = agent.agents[i];
    if !ca.properties.container.reservation or ca.properties.container.reservation.container_yard != Container_Yard {
      newList.push(ca);
    } else {
      if Delay_Per_Container > 0 {
        agent.delay(Delay_Per_Container);
      }
      if All {
        Container_Yard.Store(ca);
      } else {
        newList = agent.agents.slice(i, 1);
        Container_Yard.Store(ca);
        break;
      }
    }
    i++;
  }
  agent.agents = newList;
} else {
  throw_error("unknown release type: " + stringify(Release_Type));
}

return Carrier;