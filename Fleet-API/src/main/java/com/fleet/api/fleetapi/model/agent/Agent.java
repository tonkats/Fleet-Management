package com.fleet.api.fleetapi.model.agent;

import lombok.*;

import java.net.ConnectException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Agent {
    private UUID id;
    private ConnectionStatus connectionStatus;
    private int speed;
    private Coordinate currentLocation;
    private List<Trip> upcomingTrips;

    public Agent(Agent agent) {
        this.id = agent.id;
        this.connectionStatus = agent.connectionStatus;
        this.speed = agent.speed;
        this.currentLocation = new Coordinate(agent.getCurrentLocation());
        this.upcomingTrips = agent.getUpcomingTrips().stream().map(Trip::new).collect(Collectors.toList());
    }
}
