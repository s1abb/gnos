# Node Documentation Formatting Guide

This guide explains how to turn raw node code into a well-structured markdown documentation file, following the convention established in `spawn_agents.md`.

---

## Goals

- Make each node's purpose immediately clear to a reader who has never seen it.
- Separate prose explanation from code so both can be read independently.
- Preserve all original code exactly — do not refactor or omit anything.
- Add or improve inline comments to explain non-obvious logic.

---

## File Structure

Every node documentation file should follow this top-down structure:

```
1. Top-level heading & summary
2. Helper function sections (one per helper, if any)
3. Main execution logic section
   - One subsection per branch / mode (if applicable)
4. Termination & loop control section (if applicable)
```

Use `---` horizontal rules to visually separate major sections.

---

## 1. Top-Level Heading & Summary

Start with a single `#` heading using the node's name, followed by a short paragraph (2–5 sentences) that answers:

- What does this node do?
- What are its key modes or configurations (if it branches on an input)?
- What optional behaviour does it support?

If the node has multiple modes, list them as a bullet list with bold mode names and a short `—` description on the same line.

**Example:**

```markdown
# Spawn Agents Node

This node spawns agents into the simulation according to a configured spawn mode. It supports four modes:

- **Once** — spawns a fixed number of agents a single time then terminates.
- **Interval** — spawns agents repeatedly on a fixed time interval.
- **Rate** — spawns agents at a constant rate (agents per second).
- **Schedule** — spawns agents at specific datetimes defined in a schedule table.

Optionally, each spawned agent can be assigned a vehicle (road, rail, or physics-free), configured with PID and control parameters, placed on a track feature, and tagged.
```

---

## 2. Helper Function Sections

For each top-level `func` definition in the code, create a `##` section named `Helper: \`function_name\``.

Under the heading, write a one-to-three sentence prose description that explains:

- What the function does at a high level.
- What its parameters represent.
- Any notable side effects or optional behaviour.

Then place the complete function body in a fenced code block. Do not omit any lines.

**Heading format:**

```markdown
## Helper: `function_name`
```

**Example prose:**

```markdown
## Helper: `add_vehicle`

Attaches and configures a vehicle to a newly spawned agent based on the `Vehicle_Type` input.
Handles road, rail, and non-physics vehicle types. Also supports optional PID tuning, movement
control limits, track placement, and tagging.
```

If a function contains nested helper functions, include them inside the same code block — do not split them into separate sections.

---

## 3. Main Execution Logic Section

After all helpers, add a `##` section called `## Main Execution Logic`.

Write a one-to-two sentence introduction explaining how the top-level code is structured (e.g. what variable or input drives the branching).

If the node initialises any important flags or variables before branching, put those in their own small code block with a brief explanation.

### 3a. Subsections for Branches / Modes

If the main logic branches on an input (e.g. `if Mode == "X"`), create a `###` subsection for each branch.

The subsection heading should be the mode or branch name only — not the condition expression.

Under each `###` heading, write one or two sentences explaining what this branch does and any preconditions or edge cases. Then include the branch's code in a fenced code block.

**Example:**

```markdown
### Interval

Spawns agents repeatedly, pausing for a fixed `Interval` (in seconds) between each batch.
Optionally spawns an initial batch at simulation time zero.

\```java
else if Spawn_Mode == "Interval"
{
  ...
}
\```
```

Use a `---` rule between each `###` subsection to aid readability.

---

## 4. Termination & Loop Control

If the node ends with calls to `node.terminate()`, `node.delay()`, or `node.allow_loop_no_delay()`, group these into a final `## Termination & Loop Control` section. Include a brief prose intro if the logic is non-trivial, then place all remaining code in one code block.

---

## Code Block Rules

- Always use fenced code blocks (triple backticks) for all code — never bare text.
- Use the `java` language hint on every code fence for consistent syntax highlighting (the custom scripting language highlights reasonably well with it).
- Preserve original indentation and formatting exactly.
- Do not split a single function or `if/else if` chain across multiple code blocks.

---

## Inline Comments

When reformatting, review all existing inline comments and add new ones where logic is not self-evident. Comment guidelines:

| Situation | Comment style |
|---|---|
| What a block of code does | `// Brief action description` before the block |
| Why a guard condition exists | `// Reason for the check` above the `if` |
| Non-obvious fallback / default | `// Fallback to X if Y is not set` |
| Retry loop | Explain the retry trigger and delay |
| Optional behaviour gated by an input | `// Optionally ...` |

Do not add comments that merely restate what the code literally does (e.g. `// increment counter` above `count++`).

---

## Input Variable Conventions

Node inputs appear as `PascalCase` identifiers (e.g. `Vehicle_Type`, `Maximum_Count`). When referencing them in prose, wrap them in backticks. This distinguishes them from local variables (which are `snake_case`) and simulation API calls.

---

## Quick Checklist

Before saving a node documentation file, verify:

- [ ] Top-level heading matches the node's name.
- [ ] Opening summary explains purpose and modes/options without referencing code syntax.
- [ ] Every `func` has a `## Helper:` section with prose + code block.
- [ ] Every branch of the main `if/else if` chain has its own `###` subsection.
- [ ] All code blocks use the `java` language hint.
- [ ] All input variables are in backticks in prose.
- [ ] No code has been omitted, reordered, or altered.
- [ ] New comments added where logic is non-obvious; no redundant comments added.
- [ ] Sections are separated by `---` rules.
