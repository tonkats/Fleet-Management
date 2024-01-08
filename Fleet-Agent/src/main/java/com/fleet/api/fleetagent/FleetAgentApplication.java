package com.fleet.api.fleetagent;

import com.fleet.client.FleetClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FleetAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(FleetAgentApplication.class, args);
		FleetClient.connect(args);
	}

}
