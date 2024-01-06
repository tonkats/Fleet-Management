package com.fleet.api.fleetapi.model.agent;

import lombok.Data;

import java.util.Objects;

@Data
public class Coordinate {
    private float x;
    private float y;

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof Trip)) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        return (this.x == other.getX() && this.y == other.getY());
    }
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
