if Use_Probability {
  if Exit_1_Probability < 0 or Exit_1_Probability > 100 {
    throw_error("Exit 1 Probability must be between 0-100.");
  }
  if random_uniform(0, 1) <= Exit_1_Probability / 100 {
    return Exit_1;
  }
  return Final_Exit;
}

lookup = {"2":2,"3":3,"4":4,"5":5,"6":6};

if Use_Conditions
{
  // Determine number of conditions
  num_conditions = lookup[Number_Of_Exits] - 1;
  // Send agent to first exit where it's condition resolves to true
  if Exit_1_Condition
  {
    return Exit_1;
  }
  else if num_conditions >= 2 and Exit_2_Condition
  {
    return Exit_2;
  }
  else if num_conditions >= 3 and Exit_3_Condition
  {
    return Exit_3;
  }
  else if num_conditions >= 4 and Exit_4_Condition
  {
    return Exit_4;
  }
  else if num_conditions >= 5 and Exit_5_Condition
  {
    return Exit_5;
  }
  else
  {
    // No conditions resolved to true so send to final exit
    return Final_Exit;
  }
}
else
{
  // Determine number of exits
  num_exits = lookup[Number_Of_Exits];
  // Set exits array
  if Exits.len() != num_exits {
    Exits = [];
    while Exits.len() < num_exits - 1 {
      Exits.push(getvar("Exit_"+stringify(Exits.len() + 1)));
    }
    Exits.push(Final_Exit);
  }
  // Return specified exit
  return Exits[Exit_Number - 1];
}