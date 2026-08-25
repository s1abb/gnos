request = agent.properties[Location_Manager.ID].request;
agent.properties[Location_Manager.ID].item.in_use = false;

Location_Manager.Resource.release(request);



return exit;