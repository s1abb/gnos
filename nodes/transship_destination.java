if Wait {
  agent.agents += Source.Take(Quantity, Delay_Per_Container, agent);
  
  return Exit;
} else {
  agent.agents += Source.Attempt_Take(Quantity, Delay_Per_Container, agent);

  return Incomplete;
}
