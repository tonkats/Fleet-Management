package com.fleet.api.fleetapi.config;

import com.fleet.api.fleetapi.agent.AgentService;
import com.fleet.api.fleetapi.model.agent.Agent;
import com.fleet.api.fleetapi.model.agent.ConnectionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;
    @Autowired
    AgentService agentService;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String agentId = (String) headerAccessor.getSessionAttributes().get("userId");
        if (agentId != null) {
            log.info("Connection lost with agent of id: {}", agentId);
            Agent agent = Agent.builder()
                    .id(UUID.fromString(agentId))
                    .connectionStatus(ConnectionStatus.DISCONNECTED)
                    .build();
            agentService.agentDisconnected(agent);
            messagingTemplate.convertAndSend("/topic/admin.updateAgent", agent);
        }
    }

}