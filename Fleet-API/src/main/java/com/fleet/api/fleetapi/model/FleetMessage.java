package com.fleet.api.fleetapi.model;

import com.fleet.api.fleetapi.model.MessageType;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FleetMessage {
    private String content;
    private String sender;
    private MessageType type;
}
