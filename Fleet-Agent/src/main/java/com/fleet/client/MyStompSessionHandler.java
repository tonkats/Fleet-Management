package com.fleet.client;

import com.fleet.api.fleetapi.agent.FleetMessage;
import com.fleet.api.fleetapi.agent.MessageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

import java.lang.reflect.Type;

public class MyStompSessionHandler extends StompSessionHandlerAdapter {

    private Logger logger = LogManager.getLogger(MyStompSessionHandler.class);

    @Override
    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
        logger.info("New session established : " + session.getSessionId());
        session.subscribe("/topic/public", this);
        logger.info("Subscribed to /topic/public");

        FleetMessage fleetMessage = new FleetMessage();
        fleetMessage.setType(MessageType.AGENT_JOIN);
        fleetMessage.setSender("Java client");

        session.send("/app/agent.addUser", fleetMessage);
        logger.info("Message sent to websocket server");
        //session.send("/app/agent.sendMessage", getSampleMessage());
        //logger.info("Message sent to websocket server");
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        logger.error("Got an exception", exception);
    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return FleetMessage.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        FleetMessage msg = (FleetMessage) payload;
        logger.info("Received : " + msg.getContent() + " from : " + msg.getSender());
    }
}