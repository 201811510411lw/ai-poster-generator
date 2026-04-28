package com.aiposter.security;

import java.security.Principal;

public class LoginUser implements Principal {
    private final Long id;
    private final String username;
    private final String role;

    public LoginUser(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    @Override
    public String getName() {
        return username;
    }
}
