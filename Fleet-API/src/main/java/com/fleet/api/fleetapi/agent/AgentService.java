package com.fleet.api.fleetapi.agent;

import com.fleet.api.fleetapi.model.agent.Agent;
import com.fleet.api.fleetapi.model.agent.ConnectionStatus;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* This class would persist the data but keeps it in memory for this proof of concept. */
@Service
public class AgentService {

    /* Keeping all known agents in memory may not be beneficial for a very large number
     * of agents. This information is beneficially persisted in a database so that only
     * online agents or a subset of online agents are kept in memory. */
    @Getter
    private final List<Agent> knownAgents = new ArrayList<>();

    public void agentConnected(Agent agent) {
        agent.setConnectionStatus(ConnectionStatus.CONNECTED);
        knownAgents.add(agent);
    }

    public void agentDisconnected(Agent agent) {
        knownAgents.stream()
                .filter(knownAgent -> knownAgent.getId().equals(agent.getId()))
                .forEach(knownAgent -> knownAgent.setConnectionStatus(ConnectionStatus.DISCONNECTED));
    }
}
