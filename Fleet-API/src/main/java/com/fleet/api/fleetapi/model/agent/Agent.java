package com.fleet.api.fleetapi.model.agent;

import lombok.Data;

import java.net.ConnectException;
import java.util.List;
import java.util.UUID;

@Data
public class Agent {
    private UUID id;
    private ConnectionStatus connectionStatus;
    private int speed;
    private Coordinate currentLocation;
    private List<Trip> upcomingTrips;
}
