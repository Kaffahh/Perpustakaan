package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.dao.UserDao;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.PasswordHasher;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;

public class AuthController {

    private final UserDao userDao;

    public AuthController() {
        this.userDao = new UserDao();
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

        SessionManager.setCurrentUser(user);
        return user;
    }

    public void logout() {
        SessionManager.logout();
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
}