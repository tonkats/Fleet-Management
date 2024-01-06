package com.fleet.api.fleetapi.agent;

import com.fleet.api.fleetapi.model.FleetMessage;
import com.fleet.api.fleetapi.model.agent.Agent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AgentController {

    @Autowired
    AgentService agentService;

    @Autowired
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/agent.sendMessage")
    @SendTo("/topic/public")
    public FleetMessage sendMessage(
            @Payload FleetMessage fleetMessage
    ) {
        return fleetMessage;
    }

    @MessageMapping("/agent.addUser")
    @SendTo("/topic/public")
    public FleetMessage addUser(
            @Payload FleetMessage fleetMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        /* Save agent id in the web socket session */

        headerAccessor.getSessionAttributes().put("userId", fleetMessage.getSender());
        return fleetMessage;
    }

    @MessageMapping("/agent.addAgent")
    @SendTo("/topic/admin.newAgent")
    public Agent addAgent(
            @Payload Agent agent,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        agentService.agentConnected(agent);
        log.info("Agent added with speed: {}", agent.getSpeed());
        /* Save agent id in the web socket session */
        headerAccessor.getSessionAttributes().put("userId", agent.getId().toString());
        return agent;
    }

    @MessageMapping("/agent.getAgents")
    public void processCommand(@Payload FleetMessage message) {
        log.info("Sending list of agents to user: {}", message.getSender());
        messagingTemplate.convertAndSendToUser(
                message.getSender(), "/queue/agentStates",
                agentService.getKnownAgents()
        );
    }
}
