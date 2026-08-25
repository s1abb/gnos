if Carry_Type == "Yard Request" {
  if !agent.properties.container_reservations {
    agent.properties.container_reservations = [];
  }
  
  reservations = [];
  i = 0;
  for reservation in agent.properties.container_reservations {
    if reservation.tag == Tag and reservation.container_yard == Container_Yard {
      reservations.push(reservation);
    }
  }

  i = 0;
  while i < Quantity {
    request = {"tag": Tag};
    if reservations.len() > 0 {
      request = {"reservation": reservations.pop(0)};
      agent.properties.container_reservations.remove(request.reservation);
    }
    agent.agents.push(Container_Yard.Take(request));
    if Delay_Per_Container > 0 {
      agent.delay(Delay_Per_Container);
    }
    i++;
  }
} else if Carry_Type == "Yard Reserved" {
  if !agent.properties.container_reservations {
    agent.properties.container_reservations = [];
  }
  
  i = 0;
  newList = [];
  while i < agent.properties.container_reservations.len() {
    r = agent.properties.container_reservations[i];
    if r.container_yard != Container_Yard {
      newList.push(r);
    } else {
      if All {
        agent.agents.push(Container_Yard.Take({"reservation": r}));
        if Delay_Per_Container > 0 {
          agent.delay(Delay_Per_Container);
        }
      } else {
        newList = agent.properties.container_reservations.slice(i, 1);
        agent.agents.push(Container_Yard.Take({"reservation": r}));
        if Delay_Per_Container > 0 {
          agent.delay(Delay_Per_Container);
        }
        break;
      }
    }
    i++;
  }
  agent.properties.container_reservations = newList;
} else if Carry_Type == "Input Agents" {
  if agent.agent_type == Carrier_Agent_Type {
    if Quantity == null {
      throw_error("Carrier agent must have Quantity parameter set");
    }
    i = 0;
    requests = [];
    while i < Quantity {
      requests.push(Resource.request());
      i++;
    }
    
    for r in requests {
      agent.delay(r);
      ca = Containers.pop(0);
      if ca == null {
        throw_error("failed to get agent");
      }
      agent.agents.push(ca);
      if Delay_Per_Container > 0 {
        agent.delay(Delay_Per_Container);
      }
      Resource.capacity -= 1;
      Resource.release(r);
      node.current--;
    }
  
    return Exit;
  } else {
    Containers.push(agent);
    Resource.capacity += 1;
    return null;
  }
} else {
  throw_error("unknown carry type: " + stringify(Carry_Type));
}
return Exit;