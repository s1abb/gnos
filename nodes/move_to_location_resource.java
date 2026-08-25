Location = agent.properties[Location_Resource.id].item.location;
passing = [];
if Passing_Locations {
    for loc in Passing_Locations {
        passing.push(loc.Location);
    }
}
future_locations = [];
if Future_Locations {
    future_locations = Future_Locations;
}


if (agent.vehicle.set_destination(Location, passing, future_locations)) {
    agent.vehicle.run_to_destination();
}

agent.properties[Location_Resource.id].final_request = agent.properties[Location_Resource.id].item.final_resource.request();
agent.delay(agent.properties[Location_Resource.id].final_request);
return exit;
