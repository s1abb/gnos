for location in Locations {
  Location_Checkout.push({
    "location": location.Location,
    "in_use": false
  });
}

Resource = simulation.create_resource(Locations.len());