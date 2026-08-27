# Lumber Yard Tutorial

The purpose of this tutorial is to learn how the agent system works in GNOS. The agent system is used to feed agents through a series of nodes. These nodes act as events that occur on the agents and allow us to modify the state of the simulation and agent.

---

## Step 1: Creating an Agent Type

Any agent in GNOS needs to have a type. To create a new agent type, you can click and drag the "Agent Type" simulation setup node onto the logic canvas.

*[Screenshot: Create Agent Type]*

When you click on the newly created agent type, you will see the pane on the right populate with the configuration options.

*[Screenshot: Side Pane]*

Agents can have a name, parameters and functions. In this case, we will set the name of the agent to "Log", as this agent will represent each log being processed in the lumber yard.

Now that the agent type has been configured we need a method of creating agents of this type in the simulation. To do this, we will use the "Spawn Agents" node.

This node can be configured to spawn agents using various different modes. For our use case we want to spawn an agent every 10 minutes. Change the following configuration parameters to the following values, all other parameters can be left as their default value:

- **Name:** Spawn Logs
- **Spawn Mode:** Interval
- **Agent Type:** Log
- **Interval:** 10 Minutes

*[Screenshot: Spawn Logs]*

The interval value of 10 minutes means there will be a spawn event every 10 minutes. The "Number" parameter determines how many agents are spawned for each spawn event, the default value of 1 will be fine for our purposes. For "Agent Type", we have selected "Log" which is the name of the agent type node we created in the first step.

Next we can add a "Delay" node. We will delay the log agents for 1 minute to "simulate" the time it takes to cut the logs.

*[Screenshot: Cut Logs]*

And connect it to the spawn logs node by clicking the exit (yellow) port from the Spawn Logs node and dragging the connection to the entry (blue) port of the Cut Logs node. (Note: the color of the entry port will change to green when dragging the connection to indicate that the connection being created can connect to that port.)

*[Screenshot: Connect Nodes]*

This means that each agent that exits the Spawn Logs node will enter the Cut Logs node, wait there for one minute, and then exit the node. If an Agent exits a node that is not connected to another node, this will end the Agent's process.

Finally, we can run the simulation. Going to the "Run" tab on the left pane, we can select "New Run". This creates a new run configuration. We can name this "Run 1" and set the duration to 10,000 Seconds. We can leave the start time field empty.

*[Screenshot: Run Settings]*

Press the "Run" button. The simulation should begin playback, and a playback bar will appear at the bottom left of the screen.

*[Screenshot: Playback Bar]*

Here, we can select the playback speed of the simulation. This does not affect the simulation speed as the simulation is run in the background at the max speed possible. As logs are spawned, we can see them move through the Cut Log node and exit.

*[Screenshot: Node Playback]*

At the end of our simulation, we should have 17 logs moved through the cut log node. During playback you may have noticed some extra numbers appear on the Spawn Logs and Cut Logs nodes. The number on the top/left indicates the number of agents that have entered the node. The number at the bottom/center represents the number of agents currently in a node. The number on the top/right is the number of agents that have left the node. In the picture above this means that there have been 16 agents that left the Spawn Logs node, 16 agents that have entered the Cut Logs node, 1 agent currently in the Cut Logs node and 15 agents that have left the Cut Logs node.

---

## Step 2: Resources

In a typical manufacturing process, there are certain equipment and tools that are limited and required to perform a set action. We can represent these as "Resources" in the simulation. You can create a new Resource by dragging the item from the nodes menu onto the logic canvas.

A resource has a name and quantity that needs to be set.

In this case, we will call it "Saw" and will set the quantity to 2. This means that only 2 agents can be using this set of equipment at the same time.

*[Screenshot: Saw Resource]*

For an agent to use the equipment, it must request it. Resource requests cause the agent to idle until the resource becomes available to be used. In this case, the agent will queue in the request node until the resource becomes available. After an agent has finished using the resource, it can release it, allowing another agent to start using it.

*[Screenshot: Request Saw]*

*[Screenshot: Release Saw]*

*[Screenshot: Request And Release Saw]*

By adding a resource request node and a resource release node on either side of the cut log node, we can ensure that only agents with the Saw resource are allowed to be cut. This will limit the number of agents in the Cut Log node to 2 as that is the number of resources available.

If we increase the spawn rate of logs to one every 5 minutes and make them take 15 minutes to cut, we can see the result of this.

*[Screenshot: Saw Restriction]*

As the simulation runs, we see a backlog of agents in the Request Saw node begin to grow as the resource causes a bottleneck for the agents. We can also see only 2 agents are ever in the cut log node at the same time. If we increase the number of Saws to 3, then we see that the backlog isn't created as there are enough resources to satisfy demand. This allows us to start to identify bottlenecks in more complex problems.

*[Screenshot: Saw Restriction Removed]*

> **Note:** After making changes to the simulation, you should re-run the simulation by pressing the 'RUN' button.

---

## Step 3: Conditions

The flow of agents through the model can be modified by conditions. These are snippets of code that indicate which exit an agent should take from a specific node.

In this case, we have two wood-cutting processes. We want a number of logs to be cut in the first manner and then the rest to be cut in the second manner. This can be achieved by using the "Conditional" node.

*[Screenshot: Conditional Node Connections]*

We start by disconnecting the request saw from the cut log node and adding the new Conditional node between them.

By copying the original process, we can create 2 different paths for an agent to take. Both paths will require a saw resource for the agent to operate, which will provide a level of interaction between the options.

To know which path to take, we must first keep track of how many agents have used the first method. This can be done in two ways. Firstly, we can look at the number of agents that have entered the Cut Log block. Alternatively, we can make a calculation node to keep track of the number. In this case, we will use an "Input" calculation node so we can understand how to use them.

Inputs are a type of calculation node that can be changed by the simulation. This allows the value to be used in certain nodes and code snippets. We add a new input calculation node by dragging it from the node menu onto the logic canvas.

*[Screenshot: Create Input]*

We will call the node Method 1 and set the default value to 0.

*[Screenshot: Input Config]*

Now, we must track how many agents have used Method 1. We can do this using the On Enter event of the Cut Log node. The On Enter event's code is executed by agents as they are entering the node.

*[Screenshot: Change Input]*

Here we are adding the InnScript code:

```
Method_1++;
```

"Method_1" is the identifier used to get the value of the "Method 1" input calculation node. All nodes can be referenced by the sanitised version of the node's name, in this case the space in the name is converted into an underscore so Method 1 becomes Method_1. The `++;` simply adds 1 to the Method 1 input calculation node. Incrementing this whenever an agent enters the cut log node allows us to keep track of how many agents have entered this node.

Next, we can add the condition logic to the Choose Method node. Change the "Condition 1" parameter to code input view and enter the following code:

```
return Method_1 < 10;
```

*[Screenshot: Condition Code]*

This means if the value of Method_1 is less than 10, meaning less than 10 agents have entered the node, the current agent should leave through Exit 1, if not, the agent should leave through the Final Exit.

*[Screenshot: Agent Branching]*

Executing the simulation, we can see that the first 10 agents use Cut Log and the subsequent agents use the Cut Log 2 block. Since we have three Saw resources, the agents can use both blocks simultaneously as long as there are no more than three agents combined in the blocks.

---

## Step 4: Other Agents

Most processes are not limited to only 1 type of agent. Depending on what you want to simulate, introducing different agent types into a simulation will allow for more complex interactions in a larger system.

For our example, we can imagine that the logs need to be delivered by a truck. This truck may come on a set interval and drop off a set number of logs. This will require us to create a new agent type and use some nodes that allow interaction between this agent and others.

First, we add a new agent type, which we will call "Truck". Next, we will make some changes to the Spawn Logs node as we no longer need to spawn logs. Change the name from Spawn Logs to Spawn Trucks. Set the Agent Type to Truck and set the Interval to 1 hour.

*[Screenshot: Spawn Trucks]*

Connect a delay node to the spawn node and name it Travel. Set the time to 10 minutes. Create another delay node called unload and set it to 5 minutes.

*[Screenshot: Spawn and Travel]*

Finally, we need to spawn the logs from the truck. We can do this using a Split node. The Split node takes in a single agent and produces 2 different agents on the output.

There are several parameters we need to set. The "Keep Original" checkbox allows us to configure if the Agent 1 exit should produce the original agent that entered the block; if this is not set, then a new type of agent can be created at the exit. The first agent type would be the type of this new agent. New Agent Type and New Agent Quantity allows us to select the type and quantity of the agent at the second exit. We will set Keep Original to true, set new agent type as Log and New agent quantity as 20. Next, we can link exit 2 to our previous process and connect exit one to another delay block for the truck leaving set to 10 minutes.

*[Screenshot: Split Node]*

Our simulation should look as follows:

*[Screenshot: Overview]*

Running the simulation again, we can see the agents moving through the blocks as the trucks arrive. Try adjusting the saw resource to see how many would be required to satisfy the current rate of trucks, or adjust the truck rate until it meets the number of saws available.

---

## Step 5: Parameters

Once a simulation has been set up, there will be certain parameters that end users will want to change to affect the outcome of the simulation.

These can be created as simulation parameters that allow users to easily modify them when running the simulation. For this example, we will create 2 parameters: Cutting Time and Truck Interval. These will allow us to adjust these factors easily when running the simulation.

Start by adding 2 parameters from the calculation node menu.

*[Screenshot: Create Parameters]*

Name the first Truck Interval and set its Main input to 1.

*[Screenshot: Truck Interval]*

Name the second parameter Cutting Time and set its Main input to 10.

*[Screenshot: Cutting Time]*

Now, in the spawn trucks block, change the current interval and instead select the truck interval from the menu.

*[Screenshot: Use Truck Interval]*

Also use the Cutting Time parameter for the Delay Duration on both of the "Cut Logs" nodes.

*[Screenshot: Use Cutting Time]*

Finally, in the simulation run menu within the simulation run dropdown you will notice that there is another "parameters" dropdown.

If you open this dropdown you will see a list of inputs for each of your parameters. You can set these values before running the simulation to change the outcome. If left blank, they will use the default values.

*[Screenshot: Sim Run Parameters]*

Play around with these values and observe the change in result.

---

## Step 6: Metrics

Simulations rely heavily on outputs to help people understand how the model has operated and draw conclusions. In this step, we will create a key output and demonstrate how to chart it in the simulation graphs page.

### Throughput Metric

First, create a new input calculation node called Logs Cut. Set the default value to zero. Each time a log enters the release saw block, we can increment this variable using InnScript:

```
Logs_Cut++;
```

*[Screenshot: Logs Cut]*

Now, create a Metric simulation setup node. This node will allow us to record this variable for charting at a later point.

*[Screenshot: Create Metric]*

Configure the node as follows:

- **Name:** Throughput
- **Metric Type:** Timeseries
- **Value:** Logs Cut
- **Save Interval:** 1 Minute

*[Screenshot: Throughput Metric]*

Logs Cut is the value we are interested in, and we want it to record that value every minute of simulation time.

Finally, go to the graphs page:

*[Screenshot: Graphs Tab]*

And add a new graph tab by clicking the green plus button. Then add a new Timeseries graph by selecting it from the + graph dropdown. Resize the graph by dragging the bottom right corner.

*[Screenshot: Add Graph]*

Select the throughput metric and run the simulation.

*[Screenshot: Select Metric]*

This chart should now display the results of the metric. Users can download the results as a csv using the download button at the top of the chart.

*[Screenshot: Throughput Results]*

### Queuing Metric

Another metric we can track is how many logs are queuing to be cut with the Saw. All waiting logs sit in the "Request Saw" node, so recording how many are currently in that node allows us to keep track of the queue size.

We create a new metric node called "Queue". We change the value input to code view and enter the InnScript code:

```
return Request_Saw.current;
```

This records the current agents in the Request Saw node. The interval can also be set to 1 Minute.

*[Screenshot: Queue Metric]*

This metric will now be available on the graphs page alongside your "Throughput" metric. You will need to rerun the simulation to get results any time a new metric is added.

*[Screenshot: Combined Metrics]*

### Using Node Metrics

The Queue and Throughput metrics we have created both use the Metric simulation setup node. But it is also possible to create metrics from the "Nodes" tab found on event nodes and calculation nodes. Both methods achieve a similar end result, when to use them depends on what workflow you prefer. You can skip the rest of this step or continue if you would like to learn how to use node metrics to configure our Throughput and Queue metrics.

**Calculation Node Metric**

To convert the Throughput metric into a calculation node metric we start by selecting the Logs Cut node, navigating to the Metrics tab and ticking the "Track metric" box. Here we want to set the chart label to "Throughput" and the interval to 1 minute. You can now delete the old "Throughput" metric node.

*[Screenshot: Logs Cut Metric]*

Re-run the simulation and select the new "Throughput" metric to see the graph results. They should be identical to how it appeared before with the only difference being where we are configuring the metric.

**Event Node Metric**

To convert the Queue metric into an event node metric we start by selecting the Request Saw node, navigating to the Metrics tab and clicking the "Add Tracked Metric" button. Here we want to set the static variable we are recording to "Current", this indicates we want to record the current number of agents in the event node and achieves the same result as the code we included in the Metric node's value input `return Request_Saw.current;`. Also change the chart label to "Queue" and the Interval to 1 minute. You can now delete the old "Queue" metric node.

*[Screenshot: Request Saw Metric]*

Re-run the simulation and select the new "Queue" metric to see the graph results. They should be identical to how it appeared before with the only difference being where we are configuring the metric.

---

## Step 7: Optimisation

Now that we have input parameters and a measurable output (Logs Cut), we can begin optimising the system by adjusting inputs to improve a specific outcome. Doing this manually can be tedious and time-consuming so instead let's automate this process using a bulk run. A bulk run allows us to automatically generate and execute multiple simulation runs while systematically varying one or more input parameters.

First navigate to the bulk run page:

*[Screenshot: Bulk Run Page]*

Add a new bulk run by clicking the "+ BULK RUN" button in the top right. Call this bulk run "Optimise Truck Interval" and select Run 1 from the sim run dropdown. By selecting Run 1 we are using that sim run as a base for our bulk run optimisation. This means each generated run will inherit:

- Run duration
- Start time
- Parameter values (unless overridden by the bulk run inputs)

*[Screenshot: Create Bulk Run]*

Now let's add Truck Interval as an input we want to adjust in our bulk run configuration. Select it from the Parameter dropdown in the Inputs section of the bulk run configuration menu and click the add input button. Change the type to "Step". Then enter the following values:

- **Min:** 0.8 hours
- **Max:** 1.5 hours
- **Step:** 0.1 hours

This will generate 8 sim runs based on the Run 1 configuration with the value of Truck Interval ranging from 0.8 hours to 1.5 hours in steps of 0.1 hours.

We also need to specify what outputs we want to record. Select "Logs Cut" from the Variable dropdown in the Outputs section of the bulk run configuration form then click the add output button. Set the target to "Maximize". This tells GNOS to rank the generated simulation runs by the final value of Logs Cut, from highest to lowest in the results table.

Your configuration should look like the following:

*[Screenshot: Bulk Run Config]*

Close the configuration form and click the run button next to your bulk run.

*[Screenshot: Run Optimiser]*

This will generate and run 8 sim runs in the background.

Open the results modal by clicking on the eye icon next to your bulk run. There are 2 tabs, table and graph. The table tab displays a list of the sim runs that were generated including their inputs and outputs. Here we can see that there is a maximum of 45 logs cut and no difference in the number of logs cut below the 1 hour value for the truck interval. This indicates that increasing truck arrivals beyond this point no longer improves throughput and that another part of the system (for example, cutting time or saw availability) has become the limiting bottleneck.

*[Screenshot: Bulk Run Table]*

Switch to the graph tab in the results modal and click the Edit Chart Options button. Set the x-axis value to Truck Interval and the Y-axis value to Logs Cut then click save.

You should now see a line graph of the truck interval mapped against the number of logs cut. This visualisation makes it easy to see where decreasing the truck interval stops providing any benefit, reinforcing the bottleneck identified in the results table.

*[Screenshot: Bulk Run Graph]*

Using bulk runs allows us to quickly explore the behaviour of the system and identify where further improvements are possible. Once a bottleneck is found, we can repeat the optimisation process using a different parameter or output until the system meets our desired performance goals.

---

## Step 8: Make it your own

The process modeled in this example is very simple. Real simulations are typically much more detailed. Have a go at implementing the following aspects to get a hang of how to make more complicated simulations.

- Try adding a new parameter to change the number of Saw resources available. Using this new parameter optimise the number of saws required using a bulk run.
- When the logs are cut, planks are produced. Try to make the planks a new part of the process. They may need to go through another process involving new resources.
- The end product needs picking up. Maybe the same trucks dropping them off pick them up at the end of the process.
- Logs require Saws to be cut, but they may also require a workbench. Try modeling limited workspace for log cutting. Try optimising the number of workbenches using a bulk run.
- Add new metrics identifying important parts of the process that users would be interested in, e.g. Costs, Income, Profits.