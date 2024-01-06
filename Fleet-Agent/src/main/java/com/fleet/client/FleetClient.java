package com.fleet.client;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import com.fleet.api.fleetapi.model.agent.Agent;
import com.fleet.api.fleetapi.model.agent.Coordinate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

/**
 * Stand alone WebSocketStompClient.
 *
 */
public class FleetClient {

    private static String URL = "ws://localhost:8080/ws";
    public static Agent agent;
    public static void main(String[] args) {
        generateAgent();
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        stompClient.connectAsync(URL, sessionHandler);

        new Scanner(System.in).nextLine(); // Don't close immediately.
    }

    private static void generateAgent() {
        Random random = new Random();
        Coordinate startPos = new Coordinate();
        startPos.setX(random.nextFloat(100));
        startPos.setY(random.nextFloat(100));

        agent = new Agent();
        agent.setId(UUID.randomUUID());
        agent.setSpeed(random.nextInt(1, 6));
        agent.setCurrentLocation(startPos);
        agent.setUpcomingTrips(new ArrayList<>());
    }
}