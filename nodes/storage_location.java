agent.properties[id] = Tag;
for item_request in requests {
  if agent.properties[id] == item_request["tag"] or !item_request["tag"] {
    item_request["quantity"] -= 1;
    if item_request["quantity"] == 0 {
      requests.remove(item_request);
    }
    agent.properties[item_request["node_id"]] = item_request["id"];
    return exit;
  }
}

item = {
  "event": simulation.create_event(),
  "tag":agent.properties[id],
  "agent": agent
};

if !(item.tag in tag_contents) {
  tag_contents[item.tag] = 0;
}
tag_contents[item.tag] += 1;

contents.push(item);
agent.delay(item["event"]);
contents.remove(item);

tag_contents[item.tag] -= 1;

return exit;


