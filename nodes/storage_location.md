# Storage Location Node

This node manages a pool of stored agents tagged by item type, accepting agents as they enter and either immediately fulfilling a pending outbound request or holding the agent until a compatible request arrives. Each incoming agent is stamped with `Tag`, then matched against any waiting requests by tag. If a match is found the agent is dispatched to the requesting node right away; otherwise the agent waits in storage until a compatible request is issued.

---

## Main Execution Logic

On entry the agent is tagged, then the node tries to satisfy an existing request. If no request matches, the agent joins the storage pool and waits.

### Tag and Check for a Pending Request

The agent is stamped with `Tag` so that downstream logic can identify it. The node then iterates over `requests` looking for the first entry whose `tag` matches the agent's tag, or whose `tag` is unset (meaning it will accept any item). If a match is found, that request's quantity is decremented, the request is removed when exhausted, the agent's property is set to the requesting node's id so it can be routed back, and the node exits immediately.

```java
// Stamp the agent so its item type is visible to requesters and storage queries
agent.properties[id] = Tag;
for item_request in requests {
  // Accept if the request targets this specific tag, or has no tag constraint
  if agent.properties[id] == item_request["tag"] or !item_request["tag"] {
    item_request["quantity"] -= 1;
    if item_request["quantity"] == 0 {
      // Remove exhausted request so it is not matched again
      requests.remove(item_request);
    }
    // Record the requesting node's id on the agent so it routes to the right destination
    agent.properties[item_request["node_id"]] = item_request["id"];
    return exit;
  }
}
```

---

### Wait in Storage

If no pending request matched, the agent is wrapped in a record that holds an event handle, its tag, and a reference to the agent itself. The tag count in `tag_contents` is incremented so that callers can use `check_quantity` to inspect available stock by tag. The agent then delays on its event until a future `request()` call triggers it. After being woken, the agent is removed from `contents` and the tag count is decremented before the node exits.

```java
item = {
  "event": simulation.create_event(),
  "tag":agent.properties[id],
  "agent": agent
};

// Ensure the tag bucket exists before incrementing
if !(item.tag in tag_contents) {
  tag_contents[item.tag] = 0;
}
tag_contents[item.tag] += 1;

// Park the item and block until a request triggers its event
contents.push(item);
agent.delay(item["event"]);
contents.remove(item);

// Update stock count now that this item has been claimed and is leaving storage
tag_contents[item.tag] -= 1;

return exit;
```
