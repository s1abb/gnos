# Load From Storage Node

This node loads tagged items from connected storage resources onto agents as they pass through. It supports two agent roles: a **Loading Agent** that requests items from storage and waits until the full `Load_Quantity` is collected, and a **Loaded Agent** (an arriving item) that is assigned to the waiting loading agent that requested it. When `Request_Only` is active, the loading agent issues a single bulk request without blocking for fulfillment.

---

## Main Execution Logic

The node branches on `agent.agent_type`, routing loading-type agents through the item-request and wait loop, and routing loaded-item agents through the delivery path.

### Loading Agent

When an agent of type `Loading_Agent_Type` enters — or when `Request_Only` is active — a UUID is generated and a tracking entry is created in `loading_agents`, holding an event handle, the agent reference, and in-flight (`loading_count`) and cumulative (`total_loaded`) counters.

In standard mode, the outer `while` loop repeats until `total_loaded` reaches `Load_Quantity`. Each iteration caps the current batch at `Load_Request_Size` or the remaining quantity, whichever is smaller. The inner loop walks `Item_Storage` in order, preferring storages that already have available stock; if none report stock, requests are sent to all storages as a fallback to queue demand upstream. After each batch of requests, the agent delays on its event until items arrive. Once fully loaded, the tracking entry is cleared and `node.current` is decremented.

In `Request_Only` mode, the full `Load_Quantity` is requested from the first storage in a single call, the loading count is updated, and the node exits immediately without waiting.

```java
if agent.agent_type == Loading_Agent_Type or Request_Only {
  id = generate_uuid();
  // Register this loading agent so arriving items can locate it by id
  loading_agents[id] = {
    "event": simulation.create_event(),
    "agent": agent,
    "loading_count": 0,  // items in-flight (requested but not yet delivered)
    "total_loaded": 0
  };
  if !Request_Only {
    while loading_agents[id].total_loaded < Load_Quantity {
      remaining = Load_Quantity - loading_agents[id].total_loaded;
      request_amount = Load_Request_Size;
      // Shrink the batch to avoid over-requesting on the final iteration
      if remaining < Load_Request_Size {
        request_amount = remaining;
      }
      while loading_agents[id]["loading_count"] < request_amount and loading_agents[id].total_loaded < Load_Quantity {
        found = false;
        // Prefer storages that already have available stock
        for storage in Item_Storage {
          storage = storage.Storage;
          if storage.check_quantity(Loading_Tag) {
            // node_id + id identify the requester so the storage can call back to the right agent
            storage.request(Loading_Tag, 1, node_id, id);
            loading_agents[id]["loading_count"]++;
            found = true;
            stay =  loading_agents[id]["loading_count"] < request_amount and loading_agents[id].total_loaded < Load_Quantity;
            if !stay {
              break;
            }
          }
        }
        if !found {
          // No storage has stock; request from all anyway to queue demand upstream
          for storage in Item_Storage {
              storage = storage.Storage;
              storage.request(Loading_Tag, 1, node_id, id);
              loading_agents[id]["loading_count"]++;
          }
        }
      }
      if loading_agents[id].total_loaded < Load_Quantity {
        agent.delay(loading_agents[id]["event"]);
      }
    }
    loading_agents[id] = null;
    // Release this agent's reserved capacity slot in the node
    node.current -= Load_Quantity;
  } else {
    Item_Storage[0].Storage.request(Loading_Tag, Load_Quantity, node_id, id);
    loading_agents[id]["loading_count"] += Load_Quantity;
  }
  return exit;
}
```

---

### Loaded Agent

When an agent of type `Loaded_Agent_Type` enters, it carries a property keyed by `node_id` whose value is the UUID of the loading agent that requested it. The loaded item is pushed onto that loading agent's agents list, `loading_count` is decremented, `total_loaded` is incremented, and the event is triggered to resume the loading agent's wait loop.

```java
} else if agent.agent_type == Loaded_Agent_Type {
  if agent.properties[node_id] {
    loading_agent = loading_agents[agent.properties[node_id]];
    if loading_agent {
      loading_agent["loading_count"]--;
      loading_agent["total_loaded"]++;
      loading_agent["agent"].agents.push(agent);
      loading_agent["event"].trigger();
    }
  }
}
```
