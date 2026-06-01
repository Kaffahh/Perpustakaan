package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    public User findByUsernameAndPassword(String username, String password) throws SQLException {
        String sql = "SELECT id_user, username, nama_lengkap, email, password, role, created_at FROM users WHERE username = ? AND password = ? LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapUser(resultSet);
            }
        }
    }

    public User findById(int idUser) throws SQLException {
        String sql = "SELECT id_user, username, nama_lengkap, email, password, role, created_at FROM users WHERE id_user = ? LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapUser(resultSet);
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        int idUser = resultSet.getInt("id_user");
        String username = resultSet.getString("username");
        String nama = resultSet.getString("nama_lengkap");
        String email = resultSet.getString("email");
        String password = resultSet.getString("password");
        String role = resultSet.getString("role");
        String createdAt = resultSet.getString("created_at");

        return new User(idUser, nama, email, username, password, role, createdAt);
    }
}