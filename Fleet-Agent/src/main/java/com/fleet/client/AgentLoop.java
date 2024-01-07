package com.fleet.client;

import com.fleet.api.fleetapi.model.agent.Agent;
import com.fleet.api.fleetapi.model.agent.Coordinate;
import com.fleet.api.fleetapi.model.agent.Trip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompSession;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AgentLoop {

    private final float distanceUpdateResolution = 0.2f;
    private final float distanceResolution = 0.001f;
    private final float timeMultiplier = 100.0f;
    private final float moveDistance = (float) FleetClient.agent.getSpeed() / 1000;
    private final static Coordinate lastUpdatedPosition = new Coordinate();
    private StompSession session;

    private Logger logger = LogManager.getLogger(AgentLoop.class);

    public AgentLoop(StompSession session) {
        this.session = session;
    }

    public void runLoop() {

        while (FleetClient.agent.getUpcomingTrips().isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Thread Interrupted");
            }
        }
        Trip currentTrip = FleetClient.agent.getUpcomingTrips().remove(0);
        try {
            LocalDateTime date = LocalDateTime.now();
            long currentTimeSeconds = Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).getSeconds();
            long timeToWait = currentTrip.getDepartureTime() - currentTimeSeconds;
            if (timeToWait > 0) {
                Thread.sleep(timeToWait * 1000);
            }
            while (!currentTrip.getRoute().isEmpty()) {
                Coordinate nextPos = currentTrip.getRoute().remove(0);
                moveToPoint(nextPos);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void moveToPoint(Coordinate c) throws InterruptedException {
        while (FleetClient.agent.getCurrentLocation().distance(c) > distanceResolution) {
            FleetClient.agent.getCurrentLocation().moveTowards(c, moveDistance);
            Thread.sleep((long) (1000.0f / timeMultiplier));
            if (lastUpdatedPosition.distance(FleetClient.agent.getCurrentLocation()) > distanceUpdateResolution) {
                // TODO: Only send the position changes to save bandwidth.
                updateAgentState(session, FleetClient.agent);
            }
        }
    }

    public void positionUpdated() {
        Coordinate agentLocation = FleetClient.agent.getCurrentLocation();
        lastUpdatedPosition.setX(agentLocation.getX());
        lastUpdatedPosition.setY(agentLocation.getY());
    }

    public void updateAgentState(StompSession session, Agent agent) {
        session.send("/app/agent.updateAgent", agent);
        logger.info("Sent agent update");
    }
}
