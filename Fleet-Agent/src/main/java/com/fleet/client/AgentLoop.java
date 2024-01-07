package com.fleet.client;

import com.fleet.api.fleetapi.model.agent.Agent;
import com.fleet.api.fleetapi.model.agent.Coordinate;
import com.fleet.api.fleetapi.model.agent.Trip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompSession;

import java.time.Duration;
import java.time.LocalDateTime;

public class AgentLoop implements Runnable {

    private final float distanceUpdateResolution = 0.5f;
    private float moveDistance = (float) FleetClient.getAgent().getSpeed() / 10;
    private final static Coordinate lastUpdatedPosition = new Coordinate();
    private StompSession session;

    private Logger logger = LogManager.getLogger(AgentLoop.class);

    public AgentLoop(StompSession session) {
        this.session = session;
    }

    public void run() {
        while (true) {
            while (FleetClient.getAgent().getUpcomingTrips().isEmpty()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread Interrupted");
                }
            }

            Trip currentTrip = FleetClient.getAgent().getUpcomingTrips().get(0);
            Trip currentTripCopy = new Trip(currentTrip);
            try {
                LocalDateTime date = LocalDateTime.now();
                long currentTimeSeconds = Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).getSeconds();
                long timeToWait = currentTrip.getDepartureTime() - currentTimeSeconds;
                while (timeToWait > 0) {
                    Thread.sleep(1000);
                    if (FleetClient.getAgent().getUpcomingTrips().isEmpty()) break;
                    currentTrip = FleetClient.getAgent().getUpcomingTrips().get(0);
                    if (!currentTrip.equals(currentTripCopy)) break;
                    date = LocalDateTime.now();
                    currentTimeSeconds = Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).getSeconds();
                    timeToWait = currentTrip.getDepartureTime() - currentTimeSeconds;
                }
                if (FleetClient.getAgent().getUpcomingTrips().isEmpty()) continue;
                while (!currentTrip.getRoute().isEmpty() && currentTripCopy.equals(FleetClient.getAgent().getUpcomingTrips().get(0))) {
                    Coordinate nextPos = currentTrip.getRoute().remove(0);
                    moveToPoint(nextPos);
                    if (!FleetClient.getAgent().getUpcomingTrips().get(0).equals(currentTripCopy)) break;
                }
                updateTripFinished(currentTripCopy);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void moveToPoint(Coordinate c) throws InterruptedException {
        Agent agent = FleetClient.getAgent();
        while (agent.getCurrentLocation().distance(c) > 0.001f) {
            agent.getCurrentLocation().moveTowards(c, moveDistance);
            Thread.sleep((long) (25.0f));
            if (lastUpdatedPosition.distance(agent.getCurrentLocation()) > distanceUpdateResolution) {
                updateAgentPosition(session, agent.getCurrentLocation());
            }
        }
        updateAgentPosition(session, agent.getCurrentLocation());
    }

    public void positionUpdated() {
        Coordinate agentLocation = FleetClient.getAgent().getCurrentLocation();
        lastUpdatedPosition.setX(agentLocation.getX());
        lastUpdatedPosition.setY(agentLocation.getY());
    }

    private void updateAgentPosition(StompSession session, Coordinate position) {
        // TODO: Only send the position changes to save bandwidth.
        lastUpdatedPosition.setX(position.getX());
        lastUpdatedPosition.setY(position.getY());
        Agent agent = FleetClient.getAgent();
        agent.setCurrentLocation(position);
        FleetClient.setAgent(agent);
        session.send("/app/agent.updateAgent", agent);
        //logger.info("Sent agent update");
    }

    private void updateTripFinished(Trip trip) {
        Agent agent = FleetClient.getAgent();
        if (agent.getUpcomingTrips().get(0).equals(trip)) {
            agent.getUpcomingTrips().remove(0);
            FleetClient.setAgent(agent);
            session.send("/app/agent.updateAgent", agent);
        }
    }
}
