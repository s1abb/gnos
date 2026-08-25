if Bound_Object_Type == "Calculation Node" {
  calc_node = simulation.get_calculation_node(Calculation_Node_Name);
  agent.object.sync(Sync_Property, calc_node, Weight);
} else if Bound_Object_Type == "Equipment" {
  equipment = simulation.get_equipment(Equipment_Name);
  agent.object.sync(Sync_Property, equipment, Weight);
}
return Exit;