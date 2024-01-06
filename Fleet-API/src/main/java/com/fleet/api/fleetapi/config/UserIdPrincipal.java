package com.fleet.api.fleetapi.config;

import java.security.Principal;

public class UserIdPrincipal implements Principal {
    private String name;

    public UserIdPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}

