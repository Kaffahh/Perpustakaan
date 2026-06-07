package com.mycompany.perpustakaan.api;

import com.mycompany.perpustakaan.model.User;

public class PersistentSession {

    private final User user;
    private final String token;
    private final String expiresAt;

    public PersistentSession(User user, String token, String expiresAt) {
        this.user = user;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public User getUser() {
        return user;
    }

    public String getToken() {
        return token;
    }

    public String getExpiresAt() {
        return expiresAt;
    }
}
