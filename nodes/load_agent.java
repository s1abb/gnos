
func addToCollector(collector, collected) {
  collector.agents.push(collected);
  if (collector.quantity <= (collector.agents.len())) {
      Collector_Queue.pop(0);
      if (agent != collector) {
        node.exited += collector.agents.len();
        node.current -= collector.agents.len();
        node.current -= 1;
        collector.jump(exit);
      }
  }
}

if (agent.type == Collected_Agents_Type) {
  if (Collector_Queue.len() > 0) {
    c = Collector_Queue[0];
    addToCollector(c, agent);
  } else {
    Collected_Queue.push(agent);
    if Call_Collector {
      if Collector_Storage_Node.Collectors.len() > 0 {
        Collector_Storage_Node.current -= 1;
        Collector_Storage_Node.exited += 1;
        collector = Collector_Storage_Node.Collectors.pop(0);
        Collector_Queue.push(collector);
        while ((Collected_Queue.len() > 0) and (Collector_Queue.len() > 0)) {
            c = Collector_Queue[0];
            collected = Collected_Queue.pop(0);
            addToCollector(c, collected);
        }
      }
      else {
        Collector_Storage_Node.Requests.push(node);
      }
    }
  }
}

if (agent.type == Collector_Type) {
  if agent.quantity == null {
    throw_error("Agent of type '" + agent.type + "' requires a 'quantity' parameter to work as a collector in the 'Load Agent' node.");
  }

  Collector_Queue.push(agent);

  while ((Collected_Queue.len() > 0) and (Collector_Queue.len() > 0)) {
    c = Collector_Queue[0];
    collected = Collected_Queue.pop(0);
    addToCollector(c, collected);
  }

  if (agent.quantity <= (agent.agents.len()) or Depart_Immediately) {
    Collector_Queue.pop(0);
    node.exited += agent.agents.len();
    node.current -= agent.agents.len();
    return exit;
  }
}


return null;