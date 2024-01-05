package com.fleet.api.fleetapi.model.agent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Trip {
    private List<Coordinate> route;
    private LocalDateTime departureTime;
}
