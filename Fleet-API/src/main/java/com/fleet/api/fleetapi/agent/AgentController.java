package com.fleet.api.fleetapi.agent;

import com.fleet.api.fleetapi.model.FleetMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class AgentController {

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
        headerAccessor.getSessionAttributes().put("agentId", fleetMessage.getSender());
        return fleetMessage;
    }
}
