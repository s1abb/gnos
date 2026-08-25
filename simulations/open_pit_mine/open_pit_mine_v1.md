Haul Trucks[Parameter]: 24

Agent Count [Input]: 0

Truck [Agent Type]
Haul Truck [Motor Vehicle]: CAT 777

Parkup Locations [Location Manager]:
Parkup 1
Parkup 2
Parkup 3
Parkup 4
Parkup 5
Parkup 6
Parkup 7
Parkup 8
Parkup 9
Parkup 10
Parkup 11
Parkup 12
Parkup 13
Parkup 14
Parkup 15
Parkup 16
Parkup 17
Parkup 18
Parkup 19
Parkup 20
Parkup 21
Parkup 22
Parkup 23
Parkup 24

Pit Locations [Location Manager]:
Pit A
Pit B
Pit C
Pit D

Dump Locations [Location Manager]:
Dump A
Dump B

Nodes:

_1 Spawn Agents [Spawn Agents]
Parameters:
    Spawn Mode: Interval
    Agent Type: return Truck;
    Number: 1
    Maximum Count: Haul Trucks
    Allow Spawn: True
    Spawn At Start: True
    Interval: 1 Minutes
    Enable Logs: False
    Spawn Vehicle: False
Events:
    On Exit:
Metrics:

Connection: _1 to _2

_2 Get Starting Location [Get Location (Location Manager)]
Parameters:
    Location Manager: Parkup Locations
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _2 to _3

_3 Add Vehicle [Add Vehicle]
Parameters:
    Vehicle Type: Road
    Name: return agent.properties.vehicle_name;
    Network Model: Model
    Track Feature: return agent.properties[Parkup_Locations.ID].item.location;
    Motor Vehicle: Haul Truck
    Vehicle Components
    Can Turn On Spot: False
    Tags:
    Setup Control: False
    Set Controller: False
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _3 to _4

_4 Get Location Location Manager [Get Location Location Manager]
Parameters:
    Location Manager: Pit Locations
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _4 to _5

_5 Release Location Location Manager [Release Location (Location Manager)]
Parameters:
    Location Manager: Parkup Locations
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _5 to _6

_6 Go to Location Location Manager [Go to Location (Location Manager)]
Parameters:
    Future Locations:
    Passing Locations:
    Location Manager: Pit Locations
Events:
    On Enter:
    On Exit:
    On Failed Exit:
Metrics:

Connection: _6 to _7

_7 Load Vehicle [Load Vehicle]
Parameters:
    Selection Method: Type
    Component Type: Haul Truck
    Source Container
        Equipment:
        Junction:
    Load Method: Load Per Component
    Load Per Component: 200 Tonne
    Use Load Duration: True
    Load Component Duration: 60 seconds
Events:
    On Enter:
    On Exit:
Metrics:
    Chart Label: Total Loaded
    Chart Suffix:
    Metric Source: Static Variable
    Variable: Total Loaded
    Record On Change: False
    Save Interval: 1 Minutes

Connection: _7 to _8

_8 Release Location Location Manager [Release Location (Location Manager)]
Parameters:
    Location Manager: Pit Locations
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _8 to _9

_9 Get Location Location Manager [Get Location Location Manager]
Parameters:
    Location Manager: Dump Locations
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _9 to _10

_10 Go to Location Location Manager [Go to Location (Location Manager)]
Parameters:
    Future Locations:
    Passing Locations:
    Location Manager: Dump Locations
Events:
    On Enter:
    On Exit:
    On Failed Exit:
Metrics:

Connection: _10 to _11

_11 Unload Vehicle [Unload Vehicle]
Parameters:
    Selection Method: Type
    Component Type: Haul Truck
    Destination Container
        Equipment:
        Junction:
    Material:
    Unload Method: Unload Per Component
    Unload Per Component: 200 Tonne
    Use Unload Duration: True
    Unload Component Duration: 60 seconds
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _11 to _12

_12 Release Location Location Manager [Release Location (Location Manager)]
Parameters:
    Location Manager: Dump Locations
Events:
    On Enter:
    On Exit:
Metrics:

Connection: _12 to _4