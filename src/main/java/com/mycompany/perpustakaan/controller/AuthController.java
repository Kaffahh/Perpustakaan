package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.api.MemberRequest;
import com.mycompany.perpustakaan.api.PersistentSession;
import com.mycompany.perpustakaan.dao.UserDao;
import com.mycompany.perpustakaan.dao.UserSessionDao;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.PasswordHasher;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;

public class AuthController {

    private final UserDao userDao;
    private final UserSessionDao userSessionDao;

    public AuthController() {
        this.userDao = new UserDao();
        this.userSessionDao = new UserSessionDao();
    }

    public User login(String username, String password) throws SQLException {
        if (isBlank(username) || isBlank(password)) {
            throw new IllegalArgumentException("Username dan password wajib diisi.");
        }

        User user = userDao.findByUsername(username.trim());
        if (user == null) {
            return null;
        }

        if (!PasswordHasher.matches(password, user.getPassword())) {
            return null;
        }

        if (user.isSuspended()) {
            throw new IllegalStateException("Akun sedang disuspend.");
        }

        SessionManager.setCurrentUser(user);
        return user;
    }

    public PersistentSession loginPersistent(String username, String password) throws SQLException {
        User user = login(username, password);
        if (user == null) {
            return null;
        }
        userSessionDao.cleanupExpiredSessions();
        return userSessionDao.createSession(user);
    }

    public User restoreSession(String token) throws SQLException {
        User user = userSessionDao.restoreSession(token);
        if (user != null) {
            SessionManager.setCurrentUser(user);
        }
        return user;
    }

    public User register(MemberRequest request) throws SQLException {
        MemberRequest safeRequest = validateRegisterRequest(request);
        if (userDao.findByUsername(safeRequest.getUsername()) != null) {
            throw new IllegalArgumentException("Username sudah terdaftar.");
        }

        String hashedPassword = PasswordHasher.hash(safeRequest.getPassword());
        return userDao.insertMember(safeRequest, hashedPassword);
    }

    public void logout() {
        SessionManager.logout();
    }

    public void logout(String token) throws SQLException {
        if (token != null && !token.isBlank()) {
            userSessionDao.revokeSession(token);
        }
        SessionManager.logout();
    }

    public void revokeAllCurrentUserSessions() throws SQLException {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            userSessionDao.revokeAllUserSessions(currentUser.getIdUser());
        }
    }

    public boolean isLoggedIn() {
        return SessionManager.isLoggedIn();
    }

    public User getCurrentUser() {
        return SessionManager.getCurrentUser();
    }

    public boolean isAdmin() {
        User user = SessionManager.getCurrentUser();
        return user != null && user.isAdmin();
    }

    public boolean isStaff() {
        User user = SessionManager.getCurrentUser();
        return user != null && user.isStaff();
    }

    public boolean isAnggota() {
        User user = SessionManager.getCurrentUser();
        return user != null && user.isAnggota();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private MemberRequest validateRegisterRequest(MemberRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Data register wajib diisi.");
        }

        String username = requireText(request.getUsername(), "Username wajib diisi.");
        String nama = normalizeName(request.getNama(), username);
        String email = normalizeEmail(request.getEmail());
        String password = requireText(request.getPassword(), "Password wajib diisi.");

        if (password.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter.");
        }

        return new MemberRequest(username, nama, email, password);
    }

    private String requireText(String value, String message) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeName(String value, String username) {
        if (isBlank(value)) {
            return username;
        }
        return value.trim();
    }

    private String normalizeEmail(String value) {
        if (isBlank(value)) {
            return null;
        }
        String email = value.trim();
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        return email;
    }
}
