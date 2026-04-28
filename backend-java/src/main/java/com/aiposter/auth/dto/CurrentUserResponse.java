package com.aiposter.auth.dto;

public class CurrentUserResponse {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String role;

    public CurrentUserResponse(Long id, String username, String nickname, String avatar, String role) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getNickname() {
        return nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getRole() {
        return role;
    }
}
