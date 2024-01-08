package com.fleet.client;

import com.fleet.api.fleetapi.model.agent.Agent;
import com.fleet.api.fleetapi.model.agent.Coordinate;
import com.fleet.api.fleetapi.model.agent.Trip;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.time.Duration;
import java.util.List;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

/**
 * Stand alone WebSocketStompClient.
 */
@Component
public class FleetClient {

    private static String URL = "ws://localhost:8080/ws";
    private static Agent agent;

    public static void connect(String[] args) {
        generateAgent();
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        StompSessionHandler sessionHandler = new MyStompSessionHandler();
        stompClient.connectAsync(URL, sessionHandler);
        new Scanner(System.in).nextLine();
    }

    private static void generateAgent() {
        Random random = new Random();
        Coordinate startPos = new Coordinate();
        startPos.setX(random.nextFloat(450) + 25);
        startPos.setY(random.nextFloat(450) + 25);

        /* TEST TRIP DATA
        Trip trip = new Trip();
        LocalDateTime date = LocalDateTime.now();
        long currentTimeSeconds = Duration.between(date.withSecond(0).withMinute(0).withHour(0), date).getSeconds();
        trip.setDepartureTime((int) currentTimeSeconds + 5);
        Coordinate coordinate = new Coordinate();
        coordinate.setX(50.0f);
        coordinate.setY(50.0f);
        List<Coordinate> route = new ArrayList<>();
        route.add(coordinate);
        trip.setRoute(route);
        List<Trip> trips = new ArrayList<>();
        trips.add(trip);
         */

        agent = new Agent();
        agent.setId(UUID.randomUUID());
        agent.setSpeed(random.nextInt(5, 11));
        agent.setCurrentLocation(startPos);
        agent.setUpcomingTrips(new ArrayList<>());
    }

    public static synchronized Agent getAgent() {
        return new Agent(FleetClient.agent);
    }

    public static synchronized void setAgent(Agent agent) {
        FleetClient.agent = new Agent(agent);
    }
}