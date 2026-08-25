reservation = Container_Yard.Reserve_Take([agent.tag])[0];
r = Resource.request();
agent.delay(r);
agent.agents.push(Container_Yard.Take({"reservation": reservation}));
agent.delay(Delay);
r.release();
return Exit;