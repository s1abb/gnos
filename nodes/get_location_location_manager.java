request = Location_Manager.Resource.request();

agent.delay(request);
location_found = false;

for item in Location_Manager.Location_Checkout {
  if item["in_use"] == false {
    location_found = item;
    break;
  }
}

if location_found == false {
  throw_error("Failed to claim location from Location Manager:", Location_Manager.name);
}


location_found["in_use"] = true;
agent.properties[Location_Manager.ID] = {
  "item": location_found,
  "request": request
};

return exit;

