package com.mycompany.perpustakaan.api;

public class AuthResponse {

    private final boolean success;
    private final String message;
    private final UserSummary user;

    public AuthResponse(boolean success, String message, UserSummary user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public static AuthResponse success(String message, UserSummary user) {
        return new AuthResponse(true, message, user);
    }

    public static AuthResponse failure(String message) {
        return new AuthResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UserSummary getUser() {
        return user;
    }
}