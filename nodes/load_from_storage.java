if agent.agent_type == Loading_Agent_Type or Request_Only {
  id = generate_uuid();
  loading_agents[id] = {
    "event": simulation.create_event(),
    "agent": agent,
    "loading_count": 0,
    "total_loaded": 0
  };
  if !Request_Only {
    while loading_agents[id].total_loaded < Load_Quantity {
      remaining = Load_Quantity - loading_agents[id].total_loaded;
      request_amount = Load_Request_Size;
      if remaining < Load_Request_Size {
        request_amount = remaining;
      }
      while loading_agents[id]["loading_count"] < request_amount and loading_agents[id].total_loaded < Load_Quantity {
        found = false;
        for storage in Item_Storage {
          storage = storage.Storage;
          if storage.check_quantity(Loading_Tag) {
            storage.request(Loading_Tag, 1, node_id, id);
            loading_agents[id]["loading_count"]++;
            found = true;
            stay =  loading_agents[id]["loading_count"] < request_amount and loading_agents[id].total_loaded < Load_Quantity;
            if !stay {
              break;
            }
          }
        }
        if !found {
          for storage in Item_Storage {
              storage = storage.Storage;
              storage.request(Loading_Tag, 1, node_id, id);
              loading_agents[id]["loading_count"]++;
          }
        }
      }
      if loading_agents[id].total_loaded < Load_Quantity {
        agent.delay(loading_agents[id]["event"]);
      }
    }
    loading_agents[id] = null;
    node.current -= Load_Quantity;
  } else {
    Item_Storage[0].Storage.request(Loading_Tag, Load_Quantity, node_id, id);
    loading_agents[id]["loading_count"] += Load_Quantity;
  }
  return exit;
} else if agent.agent_type == Loaded_Agent_Type {
  if agent.properties[node_id] {
    loading_agent = loading_agents[agent.properties[node_id]];
    if loading_agent {
      loading_agent["loading_count"]--;
      loading_agent["total_loaded"]++;
      loading_agent["agent"].agents.push(agent);
      loading_agent["event"].trigger();
    }
  }
}