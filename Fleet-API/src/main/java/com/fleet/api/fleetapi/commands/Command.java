package com.fleet.api.fleetapi.commands;

import com.fleet.api.fleetapi.model.agent.Trip;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Command {
    UUID agentId;
    Action action;
    Trip trip;
}
