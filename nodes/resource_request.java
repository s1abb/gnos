if Enable_Timeout {
  if !agent.seize_resource(Resource, Timeout) {
    return timed_out;
  }
} else {
  agent.seize_resource(Resource);
}
return exit;