# Conditional Node

This node routes an agent to one of several exits based on a configured selection strategy. It supports three modes:

- **Probability** — sends the agent to `Exit_1` with a given percentage chance, otherwise to `Final_Exit`.
- **Conditions** — evaluates each exit's condition in order and sends the agent to the first exit whose condition resolves to `true`, falling back to `Final_Exit` if none do.
- **Fixed Exit** — sends the agent to a specific, pre-selected exit chosen by index via `Exit_Number`.

`Use_Probability` and `Use_Conditions` are checked in that order, so Probability mode takes priority if both are somehow enabled, and Fixed Exit mode is the default when neither is set. All three modes share a common `lookup` table that converts the `Number_Of_Exits` input into a usable integer.

---

## Main Execution Logic

The node first checks `Use_Probability`. If not set, it builds the shared `lookup` table, then branches on `Use_Conditions` to choose between Conditions mode and Fixed Exit mode.

### Probability

Rolls a random number and sends the agent to `Exit_1` if it falls within `Exit_1_Probability` percent, otherwise sends it to `Final_Exit`. Throws an error if `Exit_1_Probability` is outside the valid 0–100 range.

```java
if Use_Probability {
  if Exit_1_Probability < 0 or Exit_1_Probability > 100 {
    throw_error("Exit 1 Probability must be between 0-100.");
  }
  if random_uniform(0, 1) <= Exit_1_Probability / 100 {
    return Exit_1;
  }
  return Final_Exit;
}
```

---

### Conditions

Evaluates `Exit_1_Condition` through `Exit_5_Condition` in order and returns the first exit whose condition is `true`. Each condition beyond the first is only checked if `Number_Of_Exits` allows for that many exits, via `num_conditions` (one less than `Number_Of_Exits`, since the final exit is always `Final_Exit` rather than a conditioned one). If no condition resolves to `true`, the agent is sent to `Final_Exit`.

```java
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
```

---

### Fixed Exit

Sends the agent to a specific exit chosen by `Exit_Number`, rather than evaluating a probability or condition. The `Exits` array is lazily (re)built whenever its length doesn't match `num_exits` — each exit node reference is fetched dynamically via `getvar("Exit_" + n)` using the sanitised exit name, with `Final_Exit` always appended as the last entry. Once built, the agent is routed to `Exits[Exit_Number - 1]`.

```java
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
```