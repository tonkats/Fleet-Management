package com.fleet.api.fleetapi.model.agent;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class Agent {
    private UUID id;
    private int speed;
    private Coordinate currentLocation;
    private List<Trip> upcomingTrips;
}
