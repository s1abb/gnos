for location in Locations {
  item = {
    "resource": simulation.create_resource(location["Queue Size"]),
    "final_resource": simulation.create_resource(1),
    "location": location.Location
  };
  resource_list.push(item);
}