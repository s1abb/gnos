for cargo in agent.agents {
  node.exited++;
  cargo.jump(Cargo);
}

agent.agents = [];

return Main;