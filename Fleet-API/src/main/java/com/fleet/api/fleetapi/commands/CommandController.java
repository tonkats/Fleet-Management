package com.fleet.api.fleetapi.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class CommandController {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/command")
    public void processCommand(@Payload Command command) {
        messagingTemplate.convertAndSendToUser(
                command.getAgentId().toString(), "/queue/commands",
                command
        );
    }
}
