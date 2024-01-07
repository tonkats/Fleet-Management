package com.fleet.api.fleetapi.model.agent;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
public class Trip {
    private List<Coordinate> route;
    private int departureTime;  // Current clock time in seconds

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Trip other)) {
            return false;
        }
        Boolean sameRoute = route.equals(other.route);
        Boolean sameTime = departureTime == other.departureTime;
        return sameRoute && sameTime;
    }
    @Override
    public int hashCode() {
        return Objects.hash(route.toString(), departureTime);
    }
}
