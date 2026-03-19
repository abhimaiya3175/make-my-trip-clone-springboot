package com.makemytrip.security;

public class AuthenticatedUser {
    private final String userId;
    private final String userName;
    private final String role;

    public AuthenticatedUser(String userId, String userName, String role) {
        this.userId = userId;
        this.userName = userName;
        this.role = role;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getRole() {
        return role;
    }
}
