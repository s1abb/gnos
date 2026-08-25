containers = [];

if Quantity == null {
  containers = agent.agents;
  agent.agents = [];
} else {
  if Quantity > agent.agents.len() {
    Quantity = agent.agents.len();
  }
  i = 0;
  while i < Quantity {
    containers.push(agent.agents.pop(0));
    i++;
  }
}

events = [];
while Destinations.len() > 0 and containers.len() > 0 {
  event = simulation.create_event();
  events.push(event);
  Destinations[0].containers.push({
    "container": containers.pop(0),
    "release": func() {
      if event.is_open {
        throw_error("opened already");
      }
      event.open();
    },
  });
  
  dest = Destinations[0];
  if dest.desired <= dest.containers.len() {
    Destinations.pop(0);
  }
  dest.release();
}


if containers.len() == 0 {
  for event in events {
    agent.delay(event);
  }
  return Exit;
}

if !Wait {
  for event in events {
    agent.delay(event);
  }
  agent.agents += containers;
  return Incomplete;
}

source = {"containers": []};
while containers.len() > 0 {
  event = simulation.create_event();
  events.push(event);
  source.containers.push({
    "container": containers.pop(0),
    "release": func() {
      if event.is_open {
        throw_error("opened already");
      }
      event.open();
    },
  });
}
Sources.push(source);

for event in events {
  agent.delay(event);
}

return Exit;