package com.fleet.api.fleetapi.store;

import com.fleet.api.fleetapi.model.agent.Agent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentStore {
    private Map<Integer, Agent> connectedAgents = new HashMap<>();

    /* Keeping all known agents in memory may not be beneficial for a very large number
    * of agents. This information is beneficially persisted in a database instead. */
    private List<Agent> knownAgents = new ArrayList<>();
}
