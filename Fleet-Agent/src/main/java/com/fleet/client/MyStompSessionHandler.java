package com.fleet.client;

import com.fleet.api.fleetapi.commands.Action;
import com.fleet.api.fleetapi.commands.Command;
import com.fleet.api.fleetapi.model.FleetMessage;
import com.fleet.api.fleetapi.model.MessageType;
import com.fleet.api.fleetapi.model.agent.Agent;
import com.fleet.api.fleetapi.model.agent.Trip;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.*;

import java.lang.reflect.Type;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {

    private Logger logger = LogManager.getLogger(MyStompSessionHandler.class);

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());

        //session.subscribe("/topic/public", this);
        subscribeToMessageTopic("/topic/public", session);
        logger.info("Subscribed to /topic/public");

        String commandTopic = "/user/" + FleetClient.agent.getId() + "/queue/commands";
        subscribeToCommandTopic(commandTopic, session);
        logger.info("Subscribed to " + commandTopic);


        FleetMessage fleetMessage = new FleetMessage();
        fleetMessage.setType(MessageType.JOIN);
        fleetMessage.setSender("Java client");

        session.send("/app/agent.addUser", fleetMessage);
        logger.info("Message sent to websocket server");

        /* AGENT */

        session.send("/app/agent.addAgent", FleetClient.agent);

        AgentLoop agentLoop = new AgentLoop(session);
        agentLoop.positionUpdated();
        agentLoop.runLoop();
        //session.send("/app/agent.sendMessage", getSampleMessage());
        //logger.info("Message sent to websocket server");
    }

    private void subscribeToMessageTopic(String topic, StompSession session) {
        session.subscribe(topic, new StompSessionHandlerAdapter() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return FleetMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers,
                                    Object payload) {
                FleetMessage msg = (FleetMessage) payload;
                logger.info("Received : " + msg.getContent() + " from : " + msg.getSender());
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                logger.error("Got an exception", exception);
            }
        });
    }

    private void subscribeToCommandTopic(String topic, StompSession session) {
        session.subscribe(topic, new StompSessionHandlerAdapter() {

            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Command.class;
            }

            @Override
            public void handleFrame(StompHeaders headers,
                                    Object payload) {
                Command command = (Command) payload;
                logger.info("Received command: " + command);
                /* If trip isn't already scheduled, add it. Otherwise, remove it */
                if (command.getAction() == Action.ADD) {
                    addTrip(command.getTrip());
                } else if (command.getAction() == Action.REMOVE) {
                    removeTrip(command.getTrip());
                }
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                logger.error("Got an exception", exception);
            }
        });
    }

    private void removeTrip(Trip trip) {
        if (!FleetClient.agent.getUpcomingTrips().remove(trip)) {
            // Throw error, trip can't be removed because it doesn't exist.
        } else {
            logger.info("Trip removed: " + trip.toString());
        }
    }

    private void addTrip(Trip trip) {
        /* TODO: Add time and position validation on current trips. */
        FleetClient.agent.getUpcomingTrips().add(trip);
        FleetClient.agent.getUpcomingTrips().sort((s1, s2) -> s1.getDepartureTime() - s2.getDepartureTime());
        logger.info("Trip added: " + trip.toString());
    }
}