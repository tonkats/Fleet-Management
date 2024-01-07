package com.fleet.api.fleetapi.model.agent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
public class Trip {
    private List<Coordinate> route;
    private int departureTime;  // Current clock time in seconds

    public Trip(Trip trip) {
        this.departureTime = trip.departureTime;
        this.route = trip.route.stream().map(Coordinate::new).collect(Collectors.toList());
    }

    public Trip() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Trip other)) {
            return false;
        }
        return route.equals(other.route);
    }

    @Override
    public int hashCode() {
        return Objects.hash(route.toString(), departureTime);
    }
}
