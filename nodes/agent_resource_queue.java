if location_list.len() == 0 {
  for location in Waiting_Locations {
    location_list.push({
      "resource": simulation.create_resource(1),
      "location": location.Location
    });
  }
}

while true {

  if requests.len() > 0 {
    first_request = requests.pop(0);
    for key in first_request.agent.properties {
      agent.properties[key] = first_request.agent.properties[key];
    }
    first_request.agent.properties[id] = {
      "parent": agent,
    };
    first_request.event.trigger();
    return;
  }
  found = false;
  for location in location_list {
    if location.resource.remaining > 0 {
      agent.seize_resource(location.resource);
      agent.properties[id] = location;
      found = true;
      break;
    }
  }
  if found {
    break;
  }
  agent.delay(location_queue);
}
if (agent.vehicle.set_destination(agent.properties[id].location, [], [])) {
    agent.vehicle.run_to_destination();
}
if requests.len() > 0 {
    first_request = requests.pop(0);
    agent.release_resource(agent.properties[id].resource);
    location_queue.trigger();
    for key in first_request.agent.properties {
      agent.properties[key] = first_request.agent.properties[key];
    }
    first_request.agent.properties[id] = {
      "parent": agent,
    };
    first_request.event.trigger();
    return;
}
agent_queue.push(agent);

