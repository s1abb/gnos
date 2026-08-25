Location = agent.properties[Location_Manager.ID].item.location;
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
    return exit;
} else {
    return failed_exit;
}
