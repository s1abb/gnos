# Charge Vehicle Node

This node charges the current agent's vehicle battery at a fixed `Rate` until it reaches a target charge `Level`, expressed as a percentage of the vehicle's total `battery_capacity`. Charging is simulated in fixed 10-second increments, with the agent's delay advancing alongside each charge step so that charge time is reflected in the simulation clock.

---

## Main Execution Logic

The node loops, charging the battery in 10-second steps, until `battery_level` reaches the target threshold (`battery_capacity * Level / 100`). It re-checks the threshold immediately after each charge step so the node can exit as soon as the target is reached, without waiting on an unnecessary trailing delay.

```java
while agent.vehicle.battery_level < agent.vehicle.battery_capacity * (Level / 100) {
  agent.vehicle.charge_battery(Rate, 10);
  if agent.vehicle.battery_level >= agent.vehicle.battery_capacity * (Level / 100) {
    return exit;
  }
  
  agent.delay(10);
}
return exit;
```