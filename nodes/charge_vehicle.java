while agent.vehicle.battery_level < agent.vehicle.battery_capacity * (Level / 100) {
  agent.vehicle.charge_battery(Rate, 10);
  if agent.vehicle.battery_level >= agent.vehicle.battery_capacity * (Level / 100) {
    return exit;
  }
  
  agent.delay(10);
}

return exit;
