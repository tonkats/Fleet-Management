package com.fleet.api.fleetapi.config;

import com.fleet.api.fleetapi.model.FleetMessage;
import com.fleet.api.fleetapi.model.MessageType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String agentId = (String) headerAccessor.getSessionAttributes().get("agentId");
        if (agentId != null) {
            log.info("Connection lost with agent of id: {}", agentId);
            var fleetMessage = FleetMessage.builder()
                    .type(MessageType.LEAVE)
                    .sender(agentId)
                    .build();
            messagingTemplate.convertAndSend("/topic/public", fleetMessage);
        }
    }

}