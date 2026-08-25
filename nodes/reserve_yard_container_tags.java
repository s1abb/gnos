if !agent.properties.container_reservations {
  agent.properties.container_reservations = [];
}

if Blocking {
  agent.properties.container_reservations += Container_Yard.Reserve_Take(Tags);
} else {
  reservations = Container_Yard.Attempt_Reserve_Take(Tags);
  if reservations != null {
    agent.properties.container_reservations += reservations;
    return Exit;
  } else {
    return Failed;
  }
}

return Exit;