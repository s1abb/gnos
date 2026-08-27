# Rail Decarbonisation Tutorial

In this tutorial, you will build up a rail network simulation step by step to explore different decarbonisation strategies. The focus is on demonstrating a clear, repeatable workflow for:

- Establishing a validated diesel baseline to confirm route feasibility and operational logic
- Introducing battery-electric and hybrid technologies only after the baseline is stable
- Adding charging infrastructure (static and moving)
- Quantitatively comparing strategies using simulation outputs
- Optimising design choices such as battery size and charging power

By the end of the tutorial, you will understand how to use simulation to answer questions like:

- Can this route be decarbonised at all?
- What infrastructure is required to make it viable?
- What are the trade-offs between energy use, cycle time, and infrastructure cost?

## What you will build

- A single route heavy-haul rail simulation
- A diesel baseline
- Quantitative metrics for cycle time and energy use
- A parameterised vehicle configuration

*[Screenshot: Overview]*

---

## Step 1: Network Model

### Simulation Network Model

Create a new simulation from the simulations page with the name "Rail Decarbonisation Tutorial" and default time unit of "Hours".

Click on the "Network Models" button located in the Nodes tab of the simulation menu to bring up the simulation network models modal.

*[Screenshot: Simulation Network Models Button]*

Click on the "Add Network Model" button to add a simulation network model. Give the new entry the name "Rail Network".

*[Screenshot: Add Sim Network Model]*

Next, we need to assign a network model to the simulation network model entry.

For this simulation we will need a rail network model that has a few distinct properties:

- A **Network Entrance** track feature where trains will enter/exit the network.
- A **Yard 1** track feature where the train will perform static charging.
- A **Loader 1** track feature where the train will be loaded with material.
- The train should be able to navigate from/to the following features:
  - Network Entrance => Yard 1
  - Network Entrance => Loader 1
  - Yard 1 => Loader 1
  - Loader 1 => Yard 1

These features represent the minimum infrastructure needed to model loading, charging, and network entry/exit while keeping the tutorial focused on energy and operations rather than network complexity.

If you already have a network model that meets these requirements, assign it to the Rail Network entry by selecting its name from the Network Model dropdown, then move on to Step 2. The remainder of this step will walk through the process of creating a new network model that encapsulates a section of the Serra Norte mining complex located in Brazil.

### Track Construction

Inside the simulation network models modal, click the green plus button next to the 'Rail Network' entry we created above. This is a faster way to create a new network model and assign it to a simulation in one step.

*[Screenshot: Create New Network Model]*

Give the new network model the name "Rail Decarbonisation Network" and make sure the track type is "Rail".

*[Screenshot: Create New Network Model 2]*

Once the network model has been created the green plus button will be replaced with an "Edit network model" button, click this to open the network model editor for our new network model.

*[Screenshot: Edit Network Model Button]*

Once the network model editor has loaded press `ctrl + f` to open the location search input. Paste in the following GPS coordinates `-6.033234, -50.143565` then click the search button. This will take you to where we will begin the construction of our network model.

*[Screenshot: Map Search]*

Start by placing an end loop in the northern area of the mining complex.

*[Screenshot: End Loop 1]*

Continue a single track section up until the yard located just outside Jardim Paraiso.

*[Screenshot: Initial Layout]*

Add another end loop at the yard.

*[Screenshot: End Loop 2]*

Add another single track section that continues away from both of the end loops created. This will be used as the entrance/exit of the network.

*[Screenshot: Network Entrance]*

### Signals

Ensure that the signals are correct for any junctions you have created. You can use the "Signal Detection" overlay as a guide to help identify any that may be incorrect.

*[Screenshot: Signal Detection]*

Below is an example of an incorrectly setup signal. The two purple lines, which represent the first end loop we created above, are on opposing sides (one is a Down Line the other is an Up Line). By having them on opposite sides we are indicating that a vehicle is able to pass from one to the other which is not correct for our desired result.

*[Screenshot: Incorrect Signal]*

To fix this we need to move both purple lines to one side and the remaining line on the other side. Do this by clicking on the "Change Side" button for the purple line on the Up Lines side.

*[Screenshot: Change Line Side]*

With the signal detection overlay enabled you can now see that the color of the point has changed from red to green.

*[Screenshot: Fixed Signal]*

### Track Features

Next we will place our track features, for this simulation we require 3:

- **Loader 1** inside the first loop.

  *[Screenshot: Loader 1 Feature]*

- **Yard 1** inside the second loop.

  *[Screenshot: Yard 1 Feature]*

- **Network Entrance** near the end of the line coming away from Yard 1.

  *[Screenshot: Network Entrance Feature]*

### Grades

We will also add some grades to our track in order to improve the accuracy of our simulation. Grades are particularly important for decarbonisation studies, as they strongly influence traction power requirements, energy consumption, and the feasibility of battery-only operation. To automatically add grades, navigate to the Grade Options dropdown in the network model settings pane on the left side of the screen. Open that menu then click the "Get Single Grades" button, this will automatically pull terrain height information from the map and add grades to the lines you have placed. The map terrain information can be noisy and may contain outliers, so let's clamp the maximum grade to smooth out any abnormalities that could cause unrealistic energy demands. Do this by inputting a grade clamp of 5% and clicking the "Limit Maximum Grade" option from the same menu. You can verify the grades by using the "Grade" overlay option, this will highlight lines with a color ranging from green (grade of 0%) to red (maximum current grade or a value defined by the Max Grade input).

*[Screenshot: Automatic Grades]*

The network model is now complete and ready for use. Save your changes before proceeding to ensure all track features, loops, and grades are retained.

---

## Step 2: Verify Network Model

Before adding operational or energy logic, we first validate that the network itself is navigable. This step intentionally uses minimal vehicle complexity so that any issues can be attributed to the network model rather than vehicle performance or logic errors. To do this we will simply spawn 1 agent, add a rail vehicle with 1 Basic Locomotive component, place the vehicle at the entrance and then navigate to each of the features and back to the exit. If the vehicle can complete this route, the network model is correctly configured for routing and signalling.

Start by adding the following simulation setup nodes:

**Agent Type**
- Name: Train

  *[Screenshot: Train Agent Type]*

**Rail Vehicle**
- Name: Baseline
- Vehicle Type: Basic Locomotive

  *[Screenshot: Basic Locomotive Vehicle]*

Next add a Spawn Agents node and call it "Spawn Train" then set the Agent Type parameter to "Train". By default this node is configured to spawn 1 agent at the start of the simulation which is exactly the behaviour we required.

*[Screenshot: Spawn Train]*

Now place an Add Vehicle node with the following configuration:

- Vehicle Type: Rail
- Network Model: Rail Network
- Track Feature: Network Entrance
- Rail Components: 1 x Baseline

Connect the Spawn Train node to the Add Vehicle node.

*[Screenshot: Add Vehicle]*

Finally add a Move To Location node with the Movement Mode set to List of Locations. Set the target locations list to the following: Yard 1 → Loader 1 → Yard 1 → Network Entrance. This sequence corresponds to a full vehicle cycle: yard → loader → yard → exit.

*[Screenshot: Move to List of Locations]*

Create a new sim run called "Baseline" with a duration of 24 hours (you may need to increase the duration of the sim run if the network you are using is very large). Run the simulation and verify that the train is able to navigate to all of the track features.

*[Screenshot: Verify Network Model]*

If the train is unable to navigate there is likely to be a bad signal configuration in one of your junctions or an extreme grade. Fix any incorrect signals and grades then try again until you can see the train navigate to all locations.

Network Model validation is successful once the train can successfully navigate to all four locations.

---

## Step 3: Baseline Logic

### Operations Logic

Now that we have a verified network model, let's add some logic to simulate the Train arriving, being loaded, then leaving. The logic is intentionally kept simple as to not distract from the extra complexity that will be added in the decarbonisation logic step. We also want to record some key metrics which we can use to compare against later.

Edit the Move To Location node we created for verifying the network model:

- Name: Move to Loader 1
- Movement Mode: Single Location
- Target Location: Loader 1

*[Screenshot: Move To Loader 1]*

After moving to the loader add a Load Vehicle node with the following configuration:

- Selection Method: All
- Load Method: Fully Load
- Use Load Duration: true
- Load Component Duration: 0.25 Minutes

*[Screenshot: Load Vehicle]*

After this we want to move to the exit then remove the vehicle from the network. Place another Move To Location node after the Load Vehicle node with these settings:

- Name: Move to Exit
- Target Location: Network Entrance
- Do Not Slow Down: true *(this setting tells the vehicle to travel without slowing down to stop at the destination)*

*[Screenshot: Move to Exit]*

Finally place a Remove Vehicle node after the Move to Exit node. Enable the "Keep Agent Alive" parameter. Then make a connection which loops back to the Add Vehicle node we created earlier.

*[Screenshot: Remove Vehicle]*

Try running the simulation. You will notice that the vehicle travels to the Loader then back to where it was spawned then repeats this process. Also notice that the vehicle does not slow down when approaching the exit, this is because we ticked the "Do Not Slow Down" option in the move to exit node. The vehicle also does not wait at the loader for very long, this is because it does not have any carriages to load so instantly moves on without loading any material. We are also still using the Basic Locomotive rail vehicle component. Let's use a more appropriate set of components for our vehicle.

### Adding Diesel Locomotives and Carriages

If you already have a rail vehicle and carriage you want to use you can skip to the [Parameterised Vehicle Configuration](#parameterised-vehicle-configuration) section.

Below we will walk you through the process of creating a rail vehicle and carriage you can use for this tutorial.

Navigate to the Rail Vehicles page and create a new component with the following configuration:

- **Name:** Decarb Demo - Diesel
- **Locomotive:** true
- **Unloaded Net Braking Ratio:** 45%
- **Loaded Net Braking Ratio:** 45%
- **Energy Type:** Diesel-Electric
- **3D Model:** EMD SD40

**Main Specifications**
- Length: 20m
- Width: 3m
- Capacity: 0kg
- Axles: 6
- Max Coupler Force: 2,500kN

**Energy System**
- Fuel Tank Size: 18,000L

**Fuel Use**

| Notch | Fuel Use |
|---|---|
| 0 | 5 L/hr |
| 1 | 50 L/hr |
| 2 | 100 L/hr |
| 3 | 250 L/hr |
| 4 | 350 L/hr |
| 5 | 450 L/hr |
| 6 | 550 L/hr |
| 7 | 675 L/hr |
| 8 | 800 L/hr |

**Davis**
- A: 2.943
- B: 89.200
- C: 0.031
- D: 0.122

**Tractive Effort Curve**

*Tractive Effort*

| Notch | Cutoff Effort | Cutoff Speed | Max Speed | Reference Points (Speed → Effort) |
|---|---|---|---|---|
| 1 | 50kN | 4km/hr | 110km/hr | 4km/hr → 50kN, 50km/hr → 10kN, 110km/hr → 2kN |
| 2 | 200kN | 4km/hr | 120km/hr | 4km/hr → 200kN, 50km/hr → 30kN, 120km/hr → 10kN |
| 3 | 380kN | 4km/hr | 120km/hr | 4km/hr → 380kN, 50km/hr → 50kN, 120km/hr → 20kN |
| 4 | 500kN | 4km/hr | 120km/hr | 4km/hr → 500kN, 50km/hr → 80kN, 120km/hr → 30kN |
| 5 | 600kN | 4km/hr | 120km/hr | 4km/hr → 600kN, 50km/hr → 110kN, 120km/hr → 40kN |
| 6 | 700kN | 4km/hr | 120km/hr | 4km/hr → 700kN, 50km/hr → 150kN, 120km/hr → 60kN |
| 7 | 750kN | 4km/hr | 120km/hr | 4km/hr → 750kN, 50km/hr → 180kN, 120km/hr → 70kN |
| 8 | 800kN | 4km/hr | 120km/hr | 4km/hr → 800kN, 50km/hr → 200kN, 120km/hr → 80kN |

*Braking Effort*

| Notch | Cutoff Effort | Cutoff Speed | Max Speed | Reference Points (Speed → Effort) |
|---|---|---|---|---|
| 1 | 500kN | 4km/hr | 120km/hr | 4km/hr → 500kN, 50km/hr → 250kN, 120km/hr → 100kN |

Now navigate to the Carriage Types page and create a new carriage type with the following configuration:

- **Name:** Decarb Demo - Carriage
- **Length:** 10m
- **Width:** 2m
- **Weight:** 20,000kg
- **Max Coupler Force:** 2,500kN
- **Capacity:** 100,000kg
- **Axles:** 4
- **Unloaded Net Braking Ratio:** 45%
- **Loaded Net Braking Ratio:** 45%
- **3D Model:** Coal Cargo

**Davis**
- A: 2.94
- B: 89.20
- C: 0.31
- D: 0.12

### Parameterised Vehicle Configuration

Return to the simulation page and change the Baseline locomotive to use our new "Decarb Demo - Diesel" rail vehicle.

*[Screenshot: Change Baseline Vehicle]*

Also place a Rail Carriage simulation setup node and give it the following configuration:

- Name: Carriage
- Carriage Type: Decarb Demo - Carriage

*[Screenshot: Carriage Type]*

To prepare for decarbonisation comparisons later in the tutorial, we now parameterise the vehicle configuration so it can be changed without modifying event logic.

Call this parameter "Vehicle Type" and set the Main input to "Baseline".

*[Screenshot: Vehicle Type]*

Create another parameter called "Num Carriages" with a Main input value of 100.

*[Screenshot: Num Carriages]*

Next change the Rail Components parameter in the Add Vehicle node to use the Vehicle Type parameter for the locomotive and add Num Carriages amount of the Carriage component. We also add a second locomotive to the tail of the consist to ensure sufficient tractive effort once the carriages are fully loaded. This mirrors common heavy-haul operations and prevents false failures caused by underpowered consists.

*[Screenshot: Dynamic Vehicle Configuration]*

If you rerun the simulation now you will notice that the vehicle carriages are now being loaded with material. This extra weight will affect the time it takes for the vehicle to travel back to the network entrance. If the vehicle fails to travel, it probably means the locomotive cannot produce enough force to move the consist. This can be fixed by adding more locomotives to the vehicle, by increasing the effort values in the vehicle's tractive effort curves or by reducing the maximum grades in the network model.

### Recording Baseline Metrics

Now that we have the event node logic setup let's record some metrics. The first metric we want to record is the average time it takes for the train to complete one cycle. In this tutorial, a cycle is defined as: vehicle added to the network → loaded at the loader → exits the network. All energy and time metrics in this tutorial are reported per cycle unless otherwise stated.

Add an Input calculation node and name it "Average Cycle Time" and set the Main input to 0. In the metrics tab of this node tick both "Track Metric" and "Record On Change" checkboxes and set Chart Suffix to "minutes".

*[Screenshot: Average Cycle Time]*

To update this value add the following InnScript code into the On Enter event of the Remove Vehicle event node:

```
// Update Cycle Time
cycle_time = agent.time_since_node(Add_Vehicle).minutes();
Average_Cycle_Time = (Average_Cycle_Time * Remove_Vehicle.exited + cycle_time) / Add_Vehicle.entered;
```

*[Screenshot: Update Cycle Time]*

This code gets the number of minutes that has elapsed since the agent entered the Add Vehicle node until it reaches the Remove Vehicle node, it then updates the average cycle time with the new average. To learn more about the default bound properties on event nodes see the Event Node wiki.

Here is a further breakdown of what the different parts of the code are doing:

- `agent.time_since_node(Add_Vehicle).minutes()` — Get the number of minutes since the agent last entered the Add Vehicle node.
- `Remove_Vehicle.exited` — represents the number of completed cycles before the current one.
- `Add_Vehicle.entered` — represents the total number of completed cycles including the current cycle.
- `(Average_Cycle_Time * Remove_Vehicle.exited + cycle_time) / Add_Vehicle.entered` — calculates a new average cycle time given the current average, the current cycle time and the number of cycles completed.

This incremental averaging avoids storing all historical cycle data, updating metrics efficiently after each completed cycle.

Next we want to record the diesel use per cycle. Start by duplicating the Average Cycle Time calculation node by selecting it and clicking the green plus icon in the top right corner of the node selection bounding box. Rename the duplicated node to "Average Diesel Use" and change the metric Chart Suffix to "L".

*[Screenshot: Duplicate Node]*

*[Screenshot: Diesel Use]*

Back in the Remove Vehicle node we want to update the On Enter code block by adding the following InnScript code:

```
// Update Diesel Use
diesel_use = agent.vehicle.diesel_capacity - agent.vehicle.diesel_level;
Average_Diesel_Use = (Average_Diesel_Use * Remove_Vehicle.exited + diesel_use) / Add_Vehicle.entered;
```

*[Screenshot: Update Diesel Use]*

Similar to how we calculate average cycle time, we first get the diesel use for the current cycle then update the Average Diesel Use node's value. This assumes each locomotive starts a cycle with a full fuel tank, appropriate for per-cycle energy comparisons.

Next head over to the simulation graphs tab. Create a new graph tab by clicking the green plus button near the top of the page and name the tab "Outputs". Add 2 Number Output graphs to the tab you just created.

The first one will be used to display the Cycle Time and will need the following configuration:

- Label: Cycle Time
- Suffix: minutes
- Metrics: Average Cycle Time
- Math Function: Last

*[Screenshot: Cycle Time Number Output]*

The second number output will be used to display the Diesel Use and will need the following configuration:

- Label: Diesel Use
- Suffix: L
- Metrics: Average Diesel Use
- Math Function: Last

*[Screenshot: Diesel Use Number Output]*

If you rerun the simulation now you should see the Diesel Use and Cycle Time metric values displayed in the 2 Number Output graphs.

*[Screenshot: Number Outputs]*

As a sanity check, try temporarily reducing the number of carriages or increasing grades and observe how both cycle time and diesel use respond.

### Conclusion and Assumptions

That concludes the baseline logic for our decarbonisation simulation. We spawn a train with carriages, move to a loading location, load the carriages with material, then move to an exit location. We are also recording some key metrics, Cycle Time and Diesel Use, which will be used later to compare against different decarbonisation strategies.

At this point, we have established a stable diesel reference that consists of:

- A validated rail network
- A repeatable operation cycle
- A diesel baseline with quantitative metrics
- A parameterised vehicle definition ready for comparison

We have made a few modelling assumptions:

- A single train operates continuously
- No passing or dispatch conflicts are modelled
- Infrastructure availability is unconstrained unless explicitly added

These assumptions are intentional and help isolate energy and charging behaviour without introducing scheduling or capacity effects.

---

## Step 4: Decarbonisation Logic

With the baseline set, we can now systematically compare alternative propulsion and charging strategies under identical network and operational conditions.

### Hybrid Locomotive

The first strategy we will investigate is replacing the diesel locomotive with a hybrid diesel/battery locomotive. If you have an existing hybrid locomotive and understand the process required to implement their fuel consuming logic, you can skip the creation of a new vehicle type.

Navigate to the rail vehicles page and duplicate the Decarb Demo - Diesel vehicle we created before. Change the following configurations:

- **Name:** Decarb Demo - Hybrid
- **Energy Type:** Hybrid-Electric

**Energy System**
- Battery Capacity: 2,000kWh
- Battery Degradation: 0%
- Min State of Charge: 5%
- Max State of Charge: 95%
- Battery to Traction Motor Efficiency: 90%
- Regen Braking Efficiency: 90%
- General Auxiliary Load: 10kW

**Charge Curve**

| Battery Percent | Charge |
|---|---|
| 0% | 200kW |
| 25% | 400kW |
| 50% | 1000kW |
| 75% | 500kW |
| 100% | 250kW |

Save the changes and return to the simulation page.

Create a new Rail Vehicle simulation setup node, call it "Hybrid" and set the Vehicle Type to "Decarb Demo - Hybrid".

Hybrid locomotives can be implemented in a number of different ways, the default behaviour will consume diesel in order to generate electrical power to alleviate some of the power drain from the battery. However it is also possible to implement your own custom fuel consumption logic. We will provide an example to use.

First create a Code Block simulation setup node and name it "Hybrid Fuel Consumer". Then paste the following code into the code block:

```
speed; // m/s
notch; // -1, 0, 1-8
effort; // kN, +ve => traction, -ve => braking
temperature; // C
auxiliary_power; // kW, includes thermal
general_auxiliary_power = fuel_system.general_aux_load; // kW, ignore
thermal_auxiliary_power = 7; // kW
charge_rate; // kW
delta_time; // s

power = speed * effort; // kW

if power > 0 {
    power = power / fuel_system.battery_to_track_efficiency; // kW
} else {
    if fuel_system.enable_regen {
      power = power * fuel_system.regen_braking_efficiency; // kW
    } else {
      power = 0;
    }
}

consumed = power * delta_time / 3600; // kWhr
thermal_auxiliary_load = thermal_auxiliary_power * delta_time / 3600; // kWhr
charged = charge_rate * delta_time / 3600; // kWhr
local_charged = fuel_system.get_charge_rate() * delta_time / 3600; // kWhr

net = charged + local_charged - consumed - thermal_auxiliary_load;

regen = max([0, -consumed]);
if net > 0 {
  unused = fuel_system.charge_battery(net / delta_time * 3600, delta_time);

  if charged > unused {
    charged -= unused;
    unused = 0;
  } else {
    unused -= charged;
    charged = 0;
  }

  if local_charged > unused {
    local_charged -= unused;
    unused = 0;
  } else {
    unused -= local_charged;
    local_charged = 0;
  }

  if regen > unused {
    regen -= unused;
    unused = 0;
  } else {
    unused -= regen;
    regen = 0;
  }
}
else if net < 0 {
  unused = fuel_system.charge_battery(net / delta_time * 3600, delta_time);
  remaining_energy_required = 0; // kWh
  if unused < 0 {
    remaining_energy_required = -unused;
    unused = 0;
  }
  if remaining_energy_required > 0 {
    diesel = 0.2603351582 * remaining_energy_required; // L
    fuel_system.add_diesel(-diesel);

    if consumed > remaining_energy_required {
      consumed -= remaining_energy_required;
      unused = 0;
    } else {
      remaining_energy_required -= consumed;
      consumed = 0;
    }
    if thermal_auxiliary_load > remaining_energy_required {
      thermal_auxiliary_load -= remaining_energy_required;
      unused = 0;
    } else {
      remaining_energy_required -= thermal_auxiliary_load;
      thermal_auxiliary_load = 0;
    }
  }
}

fuel_system.add_regen_energy(regen);
if consumed < 0 {
  consumed = 0;
}
fuel_system.add_energy_consumed(consumed + thermal_auxiliary_load);
fuel_system.add_recharged_energy(local_charged + charged);
fuel_system.set_charge_power((local_charged + charged) / delta_time * 3600);

return charged;
```

*[Screenshot: Fuel Consumer]*

This code behaves very similarly to the default hybrid fuel consumption logic but simply serves as an example for you to develop further if you wanted to.

In order for the vehicle to use this fuel consumer logic we will add a small snippet of code to the On Exit event of the Add Vehicle node:

```
if Vehicle_Type == Hybrid {
  agent.vehicle.set_fuel_consumer(Hybrid_Fuel_Consumer);
}
```

*[Screenshot: Set Fuel Consumer]*

This code checks to see if the Vehicle Type is set to use the Hybrid locomotive, if so then set the fuel consumer code block to use for all rail vehicle components on the train.

Now that we have a new locomotive type we need to create a sim run that will use it. Duplicate our existing sim run (Baseline) by clicking on the 3 dot menu located in the expanded view of the sim run information and click the "Duplicate" button.

*[Screenshot: Duplicate Sim Run]*

Rename the duplicate to "Hybrid". Open the parameter menu for the Hybrid sim run and change the Vehicle Type parameter override to "Hybrid".

*[Screenshot: Hybrid Sim Run]*

Now re-run the simulation and navigate to the simulation graphs tab.

*[Screenshot: Hybrid Output]*

The cycle time should be the same as before because none of the routing logic has changed and the tractive effort curves for both vehicles are identical. In reality different locomotives will have different configurations so you will likely see a difference in cycle time if using your own custom rail vehicle. The Diesel Use metric should appear to have reduced compared to the baseline. Try loading the Baseline sim run to confirm this change. Instead of flicking back and forth between sim runs to compare outputs, an easier way to visualise the change is to use the Comparison Sim Runs feature of simulation metrics.

With the Baseline sim run loaded, open the Comparison Sim Runs menu located next to the "Run Data" label in the top right of the simulation graphs page then select "Hybrid" from the dropdown.

*[Screenshot: Comparison Sim Run Menu]*

*[Screenshot: Hybrid Comparison]*

Now we can clearly see that the Hybrid sim run used 869L less diesel per cycle on average than the Baseline sim run did in my simulation. We can also see that there is no difference in cycle times.

Another thing to consider with hybrid locomotives is the battery state of charge (SoC) and energy captured by regenerative braking. Regenerative braking is another way for battery powered vehicles to save energy when compared to internal combustion engines. Let's add another 2 metrics to record these battery metrics.

Create 2 new Input calculation nodes:

The first input will be used to measure the average amount of captured regenerative braking energy:

- **Name:** Average Captured Regen
- **Inputs — Main:** 0
- **Metric**
  - Track Metric: true
  - Suffix: kWh
  - Record On Change: true

The second node will be used to record the average lowest SoC for the battery per cycle:

- **Name:** Average Min State Of Charge
- **Inputs — Main:** 0
- **Metric**
  - Track Metric: true
  - Suffix: %
  - Record On Change: true

Now append the following InnScript code snippet to the Remove Vehicle node's On Enter event code block.

```
// Update Captured Regen
captured_regen = agent.vehicle.captured_regen_energy;
Average_Captured_Regen = (Average_Captured_Regen * Remove_Vehicle.exited + captured_regen) / Add_Vehicle.entered;

// Update Min State of Charge
min_charge = agent.min_state_of_charge;
Average_Min_State_Of_Charge = (Average_Min_State_Of_Charge * Remove_Vehicle.exited + min_charge) / Add_Vehicle.entered;
```

*[Screenshot: Update Metrics]*

This will update the average value in the same way we do for cycle time and diesel use metrics. However `agent.min_state_of_charge` is a custom agent type parameter we will need to add and update ourselves.

Find the Train agent type node, click it, then add a new agent type parameter by clicking on the "Add Parameter" button in the right side pane. Give the new parameter a name of "min_state_of_charge" and a default value of 100.

*[Screenshot: Agent Type Parameter]*

At the start of each cycle we need to reset the recorded min SoC. Add the following InnScript code snippet to the On Enter event code block of the Add Vehicle node:

```
agent.min_state_of_charge = 100;
```

*[Screenshot: Reset Min State Of Charge]*

Next we need to update the min SoC after each vehicle time step. Create a new Trigger Code node and give it the following configuration:

- **Name:** Record Min State Of Charge
- **Code:**

```
return func() {
  agent = simulation.get_agents()[0];
  state_of_charge = agent.vehicle.state_of_charge;
  if state_of_charge < agent.min_state_of_charge {
    agent.min_state_of_charge = state_of_charge;
  }
};
```

- **Function Parameter:** leave blank
- **Trigger Method:** Interval
- **Offset:** 1 second
- **Interval:** 1 second

*[Screenshot: Record Min State Of Charge]*

This node will call the function returned from the Code parameter every 1 second. The function will get the first active agent then update the min_state_of_charge parameter if the current SoC is lower than the previous recording. This implementation assumes a single active train agent, consistent with the tutorial assumptions defined in Step 3.

Now add two more Number Output metrics.

The first will display the Min State of Charge and have the following configuration:

- Label: Min State of Charge
- Suffix: %
- Metrics: Average Min State Of Charge (%)
- Math Function: Last

*[Screenshot: Min Charge Output]*

The second number output metric will display the captured regenerative braking energy and have the following configuration:

- Label: Captured Regen
- Suffix: kWh
- Metrics: Average Captured Regen (kWh)
- Math Function: Last

*[Screenshot: Captured Regen Output]*

Re-run the Hybrid sim run. The battery metrics should update as the simulation runs.

*[Screenshot: Battery Metrics]*

From these results we can see that the vehicle battery reached a minimum SoC of 5% and there was 98kWh of energy captured from regenerative braking on average per cycle. When we set up the vehicle we set the minimum allowed SoC to 5%, which means the battery was run completely flat during our cycle. To understand more about what is happening to the battery during a cycle we can use the Vehicle Performance metric graph.

Create another graph tab and name it "Vehicle Performance". Add a Vehicle Performance graph to the new graph tab, resize it to fill more of the page. Select "Train 1" from the Select Vehicle dropdown (this represents the train that completed the first cycle in the simulation). Also select State of Charge (%), Captured Regen Energy (kWh) and Fuel Use (L) from the Select Data dropdown.

*[Screenshot: Vehicle Performance Config]*

Because we know the average cycle time is 96 minutes I will also edit the chart range to display the first 100 minutes of recorded data.

*[Screenshot: Chart Range]*

The results from this graph tell the full story of how fuel use, battery SoC and captured regen energy interact with each other.

*[Screenshot: Hybrid Vehicle Performance]*

Battery energy is consumed completely before any fuel is consumed. Regenerative braking is used to recharge the battery. The battery eventually runs flat around the 70th minute then fuel use begins to rise.

### Battery Electric Locomotive

Next we will add a 3rd locomotive type, Battery-Electric, and another sim run to test the effectiveness of a battery electric locomotive compared to our hybrid and diesel baselines.

Head back to the rail vehicles page. Start by duplicating the Decarb Demo - Hybrid vehicle type. Change the config to the following values:

- **Name:** Decarb Demo - Battery
- **Energy Type:** Battery-Electric

**Energy System**
- Battery Capacity: 4,000 kWh

Save the rail vehicle type then head back to the simulation page. Create another Rail Vehicle simulation setup node with the name "Battery" and Vehicle Type "Decarb Demo - Battery".

*[Screenshot: Battery Rail Vehicle]*

Duplicate the Hybrid sim run, rename the duplicate to "Battery" and update the Vehicle Type run parameter to "Battery".

*[Screenshot: Battery Sim Run]*

Run the new "Battery" sim run now then load the "Baseline" sim run playback data. Add the "Battery" sim run to the selected Comparison Sim Runs. The output metrics show that there was a 100% reduction in diesel use compared to our diesel baseline, cycle time remains unchanged, and there was an increase in the amount of captured regenerative braking energy.

*[Screenshot: Battery Comparison]*

At this stage we have demonstrated the process of decarbonisation by replacing diesel locomotives with hybrid or battery-electric alternatives. We have demonstrated that battery-electric operation is energy-feasible, but not yet operationally sustainable under the current infrastructure assumptions.

### Static Charging

Up to this point, battery usage has been unconstrained by infrastructure. Sending a train away at <25% SOC is not a very feasible solution. Let's introduce some charging systems to investigate the requirements and feasibility of battery assets in this scenario.

To ensure there is enough battery charge in our locomotives we will add charging infrastructure at Yard 1 and configure the vehicle to charge while stationary, simulating depot charging.

Start by creating a new parameter "Static Charging Enabled" with a default value of false. A default value of false ensures we do not affect the diesel baseline logic. It is important to note that all parameters should be configured with a default value that will not affect the result of the baseline simulation run.

*[Screenshot: Static Charging Parameter]*

Add another parameter called "Max Static Charge Rate", set the suffix to "kW" and the default value to 1000. This will not affect the baseline simulation so the default can be set to a reasonable default maximum charge rate.

*[Screenshot: Max Static Charging]*

Now add a Conditional node in between the Add Vehicle and Move to Loader 1 nodes. Name it "Is Static Charge Enabled" and set Condition 1 to "Static Charging Enabled". Connect the "Final Exit" (bottom yellow port of the conditional node) to the entry of the Move to Loader 1 node. This will tell the agent to follow the charging branch when static charging is enabled or bypass the charging logic and head directly to Loader 1.

*[Screenshot: Static Charging Conditional]*

Add another Move To Location node called "Move to Yard 1" with a single target destination set to "Yard 1". Connect the entry port of this node (blue port) to Exit 1 of the Is Static Charge Enabled node (yellow port at the top of the node).

*[Screenshot: Move To Yard 1]*

After the Move To Yard 1 node add a Charge Vehicle node. Set the Rate to "Max Static Charge Rate" and keep the level at 100%. Connect the exit of the Charge Vehicle node to the entry of the Move to Loader 1 node.

*[Screenshot: Charge Vehicle]*

Finally we need to duplicate this logic and insert it after the Load Vehicle node. Copy the Is Static Charge Enabled, Move To Yard 1 and Charge Vehicle nodes and connect the copies up like so: Load Vehicle → Is Static Charge Enabled (copy) → Move To Yard 1 (copy) → Charge Vehicle (copy) → Move to Exit. Also include the connection from Is Static Charge Enabled (copy) that bypasses the charging logic and goes directly to the Move to Exit node.

*[Screenshot: Static Charging 2]*

Next we need to create 2 new sim runs where we will simulate static charging. Duplicate both the Hybrid and Battery sim runs. Update the duplicate sim run names to "Hybrid + static charging" and "Battery + static charging" then change the Static Charging Enabled parameter override in both to true.

*[Screenshot: Static Charge Sim Runs]*

Run both of these sim runs. Then load the baseline playback data and add the new sim runs to the list of selected Comparison Sim Runs.

*[Screenshot: Static Charging Comparison]*

A few things are worth noting here:

- Hybrid + static charging consumes less fuel than hybrid by itself
- Both sim runs with static charging have longer cycle times
- Battery + static charging has a longer cycle time than Hybrid + static charging, indicating that the battery-electric locomotive is spending more time charging at Yard 1
- Both sim runs with static charging have slightly higher captured regen, due to the train having to come to a stop 2 extra times

Static charging is an effective method to ensure battery powered locomotives are able to maintain a large enough energy buffer to complete their operations successfully. However it comes at the cost of increased cycle time. Depending on the nature of the operation this trade-off may be fine. However if the delay caused by static charging is significant enough it may not be a feasible decarbonisation strategy and alternatives need to be considered.

### Track Electrification

Track electrification enables locomotives to draw electrical energy directly from infrastructure while moving, reducing or eliminating reliance on onboard diesel or battery energy. Unlike static charging, electrification does not increase dwell time, making it an attractive option for high-throughput operations.

In this section we will:

- Electrify part of the rail network
- Observe how hybrid and battery-electric locomotives respond
- Quantitatively compare electrification against static charging

First we need to make some changes to our network model. Open the simulation network models modal by clicking on the Network Models button in the Nodes tab of the simulation menu. Click the edit network model button next to the Rail Network entry we added in step 1. Alternatively you can edit the relevant network model from the network models page.

*[Screenshot: Simulation Network Models Button]*

*[Screenshot: Edit Network Model Button]*

First add 2 new Power Supply Groups by clicking on the "Power Supply Groups" button inside the network model settings pane then clicking the "Add New Group" button twice. Name these new power supply groups: "Moving Charge Mainline" and "Full Track Electrification".

*[Screenshot: Power Supply Group Button]*

*[Screenshot: Power Supply Groups]*

Now we need to apply these power supply groups to the relevant track sections. By pressing `ctrl + a` you can select all track sections. In the Multiple Lines configuration menu you can now apply the "Full Track Electrification" power supply group by opening the Power Supply panel and selecting the Full Track Electrification option from the dropdown. You can also use the Power Supply Groups overlay to highlight lines yellow when they contain at least one of the selected power supply group options.

*[Screenshot: Apply Full Track PSG]*

Next we want to select a portion of the track's mainline to add to the Moving Charge Mainline power supply group. Start by selecting the first line that comes out of the Loader 1 loop. Then hold the shift button and select the line just before the turn off into Yard 1. This will select all lines between and including the two lines that were clicked. By selecting a portion of the mainline for moving charging this simulates adding moving charging to the commonly traversed mainline but not to the yards or loaders. From the Multiple Lines menu open the Power Supply panel then add the "Moving Charge Mainline" power supply group from the dropdown.

*[Screenshot: Mainline Charge]*

Save your changes then head back to the simulation page.

Now we need to configure the power supply groups. Start by creating 3 new Parameter calculation nodes.

The first will be used to toggle full track electrification:

- **Name:** Full Track Electrification Enabled
- **Inputs — Main:** false

The second will be used to toggle partial track electrification:

- **Name:** Mainline Moving Charge Enabled
- **Inputs — Main:** false

The third will be used to set the maximum moving charge rate per locomotive:

- **Name:** Max Moving Charge Per Loco
- **Suffix:** kW
- **Inputs — Main:** 1000

Now add 2 "Configure Power Supply Group" nodes.

The first one will configure the Full Track Electrification power supply group. Set the node configuration to the following values:

- Name: Configure Full Track Electrification
- Power Supply Group: Full Track Electrification
- Max Power (code input): `return Max_Moving_Charge_Per_Loco * 2;`
- Active: Full Track Electrification Enabled
- Max Power Per Unit: Max Moving Charge Per Loco

Max power uses a code input and returns twice the value of Max Moving Charge Per Loco because we have 2 locomotives on the vehicle. If you have more locomotives per vehicle update this value accordingly.

*[Screenshot: Configure Full Track]*

The second Configure Power Supply Group node will be used to configure the Moving Charge Mainline power supply group:

- Name: Configure Mainline Charging
- Power Supply Group: Moving Charge Mainline
- Max Power (code input): `return Max_Moving_Charge_Per_Loco * 2;`
- Active: Mainline Moving Charge Enabled
- Max Power Per Unit: Max Moving Charge Per Loco

*[Screenshot: Configure Mainline Charge]*

Now we need to create 4 new sim runs for hybrid and battery locomotives to use one of each power supply group.

Duplicate the Hybrid sim run twice. Rename them to "Hybrid + full track electrification" and "Hybrid + mainline charging". Then update their sim run parameters to enable the relevant moving charge methods.

*[Screenshot: Hybrid Full Track Config]*

*[Screenshot: Hybrid Mainline Config]*

Now repeat the same process for the "Battery" sim runs.

*[Screenshot: Battery Full Track Config]*

*[Screenshot: Battery Mainline Config]*

Start by running the "Hybrid + mainline charging" sim run. Navigate to the vehicle performance graph then select Train 1.

*[Screenshot: Hybrid Mainline Metrics]*

From the graph you can see that the power supply group is able to supplement the battery while the vehicle is moving, effectively reducing the battery drain rate. However right near the end of the cycle the battery runs flat and 80L of diesel is consumed. From the outputs tab we can also see that the cycle time remains unchanged compared to the diesel baseline. This demonstrates the tradeoffs between energy requirements and cycle time when using moving charge, there is also an upfront cost associated with electrifying the track but that cost is outside of the scope of this tutorial.

Next run the "Hybrid + full track electrification" sim run and again view the vehicle performance metrics for Train 1.

*[Screenshot: Hybrid Full Track Metrics]*

Now we can see that there is no diesel consumption. The battery is able to recharge while being loaded with material, which provides enough energy for the train to complete the cycle without needing to use the diesel backup.

Add "Charge Power (kW)" to the Selected Data dropdown. Now we can see the charge power provided to the battery. Whenever there is regenerative braking the charge power is reduced due to the battery's maximum recharge rate being exceeded.

*[Screenshot: Charge Power Metric]*

Notice that there is also something odd going on with the charge power while the vehicle is stationary, it is gradually declining over the whole period. This is due to the fact that when stationary vehicles do not update their performance metrics. This is done to improve the performance of simulations and reduce unnecessary vehicle logging. However in our case we are interested in the change of charge power while the vehicle is stationary. We can override the default logging behaviour using InnScript.

In the On Exit event code of the Add Vehicle node we will add:

```
agent.vehicle.log_idle = true;
```

*[Screenshot: Vehicle Log Idle]*

Now re-run the simulation and you will see that the actual charge rate shows a number of distinct steps. This corresponds with the vehicle's charge rate curve, where maximum charge rate is limited by the current battery level.

*[Screenshot: Hybrid Full Track Metrics 2]*

Next run the "Battery + mainline charging" and "Battery + full track electrification" sim runs. Load the Battery sim run data and set comparison sim runs to "Battery + static charging", "Battery + full track electrification", "Battery + mainline charging".

*[Screenshot: Battery Comparison Metrics]*

The main differences between the battery baseline sim run and the 3 charging scenarios is Cycle Time and Min State of Charge. Like with the hybrid sim runs we can see that any type of charging will increase the Min State of Charge with full track electrification being the most effective. Static charging while potentially cheaper than electrifying the track has the trade-off of increasing the cycle time. Depending on your operational requirements you can determine which method is most appropriate for your needs.

Track electrification improves energy feasibility without affecting throughput, while full track electrification enables deep decarbonisation at the cost of infrastructure dependency.

---

## Step 5: Optimisation

Now that we have the simulation logic complete next we can focus on testing various input parameters to find an optimal configuration that will satisfy any operational constraints we may have. Capital cost, maintenance cost, and grid capacity constraints are intentionally excluded to focus on operational energy behaviour.

### Battery Size

Let's make the assumption that we do not want the vehicle battery to drop below 50% state of charge during a cycle. We will vary the size of the battery to find the optimal battery capacity to satisfy this constraint.

To override the locomotive's battery size we need to add another Parameter calculation node and some InnScript code.

Add the parameter and give it the following configuration:

- Name: Battery Capacity Override
- Suffix: kWh
- Main: leave blank or set to null

By having the default value equal to null we can configure the locomotive to use its default battery capacity unless we specify an override.

Now in the On Exit event code of the Add Vehicle node add the following InnScript code:

```
if Battery_Capacity_Override != null {
  for c in agent.vehicle.get_vehicle_components() {
    c.set_battery_capacity(Battery_Capacity_Override);
    c.set_battery(Battery_Capacity_Override);
  }
}
```

This code first checks to see if there is an override specified, if there is it updates the capacity and current battery level for every locomotive on the agent's vehicle.

*[Screenshot: Battery Capacity Override]*

Next we need to configure a new bulk run which will compare battery capacity overrides to the min state of charge.

Start by navigating to the Bulk Run tab.

*[Screenshot: Bulk Run Tab]*

Add a new bulk run by clicking the "+ Bulk Run" button in the top right of the page.

*[Screenshot: Add Bulk Run]*

Then give it the following configuration:

- **Name:** Battery Optimisation
- **Sim Run:** Battery
- **Inputs**
  - Battery Capacity Override
    - Parameter: Battery Capacity Override
    - Type: Step
    - Min: 500
    - Max: 10,000
    - Step: 500
- **Outputs**
  - Average Min State Of Charge

This bulk run will generate 20 sim runs with a battery capacity override ranging from 500kWh to 10,000kWh in 500kWh increments. It uses the parameters set in the battery baseline sim run to configure all other inputs.

*[Screenshot: Battery Optimisation]*

Run this bulk run now by clicking the arrow icon button located in the "Run" column of the bulk runs table.

*[Screenshot: Run Bulk Run]*

Once it has finished running, click on the eye icon button in the actions column to open the results modal.

*[Screenshot: View Bulk Run Results]*

Change to the graph tab of the results modal and edit the chart options to plot Battery Capacity Override on the X-axis and Average Min State Of Charge on the Y-axis.

*[Screenshot: Bulk Run Graph Config]*

The results show that the optimal battery size that satisfies our minimum 50% state of charge constraint is somewhere between 5,500kWh and 6,000kWh.

*[Screenshot: Battery Optimisation Results]*

### Charging Rate

Another assumption we may have is that we cannot have a cycle time longer than 420 minutes. We can use a bulk run to determine which static charge rate will best satisfy this constraint.

Create a new bulk run with the following configuration:

- **Name:** Charge Rate Optimisation
- **Sim Run:** Battery + static charging
- **Inputs**
  - Max Static Charge Rate
    - Parameter: Max Static Charge Rate
    - Type: Step
    - Min: 100
    - Max: 1,000
    - Step: 100
- **Outputs**
  - Average Cycle Time

This bulk run will generate 10 sim runs with a max static charging rate ranging from 100kW to 1,000kW in 100kW increments. It uses the parameters set in the Battery + static charging sim run to configure all other inputs.

*[Screenshot: Charge Rate Optimisation]*

Run this bulk run and edit the result chart options to plot Max Static Charge Rate on the X-axis and Average Cycle Time on the Y-axis.

*[Screenshot: Charge Rate Optimisation Results]*

Note that a charge rate of 100kW resulted in an Average Cycle Time of 0 minutes. This is because the vehicle was not able to complete a full cycle within the 24 hour sim run duration. If you increase the duration of the sim run you will be able to retrieve an actual result, it is worth noting however that doing so will increase the time it takes to run the simulation.

The scale on the Y-axis makes it a little bit hard to determine what the optimal input is. Let's restrict the bounds by opening the Chart Options dialog and setting a Y-axis range from 400 - 450.

*[Screenshot: Graph Axis Range]*

The results show that the optimal value for Max Static Charge Rate lies somewhere between 660kW and 680kW.

*[Screenshot: Charge Rate Optimisation Results 2]*

### Battery Size and Charging Rate

The previous two optimisation examples only change one parameter and only record one result, however it is possible to run more complicated optimisation tests using bulk runs. For the next bulk run we will try to optimise battery capacity and static charging rate at the same time.

Start by duplicating the "Charge Rate Optimisation" bulk run and changing its configuration to the following:

- **Name:** Charge Rate + Battery Size Optimisation
- **Sim Run:** Battery + static charging
- **Inputs**
  - Max Static Charge Rate
    - Parameter: Max Static Charge Rate
    - Type: Step
    - Min: 500
    - Max: 1,000
    - Step: 100
  - Battery Capacity Override
    - Parameter: Battery Capacity Override
    - Type: Step
    - Min: 4,500
    - Max: 6,500
    - Step: 500
- **Outputs**
  - Average Cycle Time
  - Average Min State Of Charge

When using multiple inputs for a bulk run, there will be a sim run generated for every possible input permutation, so it is important to try and reduce the number of input configurations to avoid generating an excessive number of sim runs. Because we ran some simple optimisation scenarios prior, we have a better understanding of the range of values we should test. We also know from the locomotive configuration that the charge curve will limit the maximum allowed charge rate to 1,000kW so there is no need to test values above that. This bulk run configuration will generate 30 sim runs.

*[Screenshot: Capacity and Charge Optimisation]*

Run this bulk run now. Because we have multiple inputs and outputs we will use the "Table" tab to view the results. Try sorting the table rows by the output columns to make it easier to find the optimal inputs.

*[Screenshot: Capacity and Charge Optimisation Results]*

Here we can see that the minimum input configuration to satisfy both constraints is:

- Max Static Charge Rate: 900kW
- Battery Capacity Override: 5,500kWh

---

## Step 6: Conclusion

### What Have We Learned

In this tutorial we built a rail decarbonisation simulation by starting with a validated diesel baseline and then introducing alternative propulsion and charging strategies in a controlled and repeatable way. By holding the network and operational logic constant, we were able to directly compare diesel, hybrid, and battery-electric locomotives using quantitative outputs rather than assumptions.

The results show that hybrid and battery-electric locomotives can significantly reduce or eliminate diesel use on this route, but that energy feasibility alone is not sufficient. Battery-based operation requires charging infrastructure to remain operationally sustainable over repeated cycles, and the choice of charging strategy introduces trade-offs between cycle time, infrastructure complexity, and energy utilisation.

Static charging provides a simple and effective way to maintain battery state of charge, but increases cycle time due to dwell at charging locations. Track electrification enables charging while in motion and can preserve throughput, but shifts the problem toward infrastructure investment and network modification. By using parameters and comparison sim runs, these trade-offs can be explored systematically without modifying event logic.

This workflow demonstrates how simulation can be used to answer practical decarbonisation questions such as whether a route can be electrified, what infrastructure is required, and how design choices affect performance. From here, the same approach can be extended to larger networks, multiple trains, cost modelling, or automated optimisation to support real-world decarbonisation decisions.

### Test Your Understanding

Here are some potential improvements or changes you can make to the simulation to test how well you understand some of the concepts introduced in this tutorial:

- Introduce another scenario that uses a combination of different kinds of locomotives (e.g. 1 Diesel, 1 Battery)
- Try randomising the number of carriages used in each cycle and see how that affects the results.
- Add another output metric that displays the average CO2 emissions produced per cycle.
- Create another bulk run that will be used to determine the maximum number of carriages that can be used for a particular set of inputs.