package com.aiposter.auth.dto;

public class LoginResponse {
    private String token;
    private CurrentUserResponse user;

    public LoginResponse(String token, CurrentUserResponse user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public CurrentUserResponse getUser() {
        return user;
    }
}
