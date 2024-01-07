package com.fleet.api.fleetapi.model.agent;

import lombok.Data;

import java.util.Objects;
import java.util.Vector;

@Data
public class Coordinate {
    private float x;
    private float y;

    public Coordinate(Coordinate coordinate) {
        this.x = coordinate.x;
        this.y = coordinate.y;
    }

    public Coordinate() {
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || !(obj instanceof Coordinate)) {
            return false;
        }
        Coordinate other = (Coordinate) obj;
        return (this.x == other.getX() && this.y == other.getY());
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public float distance(Coordinate c) {
        float distX = c.x - x;
        float distY = c.y - y;
        return (float) Math.sqrt(distX * distX + distY * distY);
    }

    public void moveTowards(Coordinate c, float dist) {
        float dX = c.x - x;
        float dY = c.y - y;
        float magnitude = (float) Math.sqrt(dX * dX + dY * dY);
        if (magnitude < dist) {
            this.x = c.x;
            this.y = c.y;
        } else {
            this.x += dX / magnitude * dist;
            this.y += dY / magnitude * dist;
        }
    }
}
