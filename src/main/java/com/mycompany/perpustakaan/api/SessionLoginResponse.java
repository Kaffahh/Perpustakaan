package com.mycompany.perpustakaan.api;

public class SessionLoginResponse {

    private final boolean success;
    private final String message;
    private final UserSummary user;
    private final String sessionToken;
    private final String expiresAt;

    public SessionLoginResponse(boolean success, String message, UserSummary user, String sessionToken, String expiresAt) {
        this.success = success;
        this.message = ApiResponseMessages.normalize(success, message);
        this.user = user;
        this.sessionToken = sessionToken;
        this.expiresAt = expiresAt;
    }

    public static SessionLoginResponse success(String message, UserSummary user, String sessionToken, String expiresAt) {
        return new SessionLoginResponse(true, message, user, sessionToken, expiresAt);
    }

    public static SessionLoginResponse failure(String message) {
        return new SessionLoginResponse(false, message, null, null, null);
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

    public String getSessionToken() {
        return sessionToken;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
