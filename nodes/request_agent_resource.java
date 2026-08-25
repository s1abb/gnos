res_agent = Agent_Resource_Queue.request(agent);
res_agent.agents.push(agent);
node.current--;
node.exited++;
res_agent.jump(exit);
