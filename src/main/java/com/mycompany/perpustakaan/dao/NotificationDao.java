package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.NotificationSummary;
import com.mycompany.perpustakaan.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    public List<NotificationSummary> findNotifications(Integer targetUserId, String role, int limit) throws SQLException {
        ensureNotificationTable();
        StringBuilder sql = new StringBuilder("SELECT tipe, judul, pesan, created_at FROM notifications WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (targetUserId != null) {
            sql.append(" AND (target_user_id = ? OR target_user_id IS NULL)");
            params.add(targetUserId);
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND (target_role = ? OR target_role IS NULL)");
            params.add(role.trim().toLowerCase());
        }
        sql.append(" ORDER BY is_read ASC, created_at DESC, id_notification DESC LIMIT ?");
        params.add(limit);

        List<NotificationSummary> notifications = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(new NotificationSummary(
                            resultSet.getString("tipe"),
                            resultSet.getString("judul"),
                            resultSet.getString("pesan"),
                            resultSet.getString("created_at")));
                }
            }
        }
        return notifications;
    }

    public void createNotification(String tipe, String judul, String pesan, Integer targetUserId, String targetRole) throws SQLException {
        ensureNotificationTable();
        String sql = "INSERT INTO notifications (tipe, judul, pesan, target_user_id, target_role) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tipe);
            statement.setString(2, judul);
            statement.setString(3, pesan);
            if (targetUserId == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, targetUserId);
            }
            statement.setString(5, targetRole == null ? null : targetRole.trim().toLowerCase());
            statement.executeUpdate();
        }
    }

    public void markAllRead(Integer targetUserId, String role) throws SQLException {
        ensureNotificationTable();
        StringBuilder sql = new StringBuilder("UPDATE notifications SET is_read = 1 WHERE is_read = 0");
        List<Object> params = new ArrayList<>();
        if (targetUserId != null) {
            sql.append(" AND (target_user_id = ? OR target_user_id IS NULL)");
            params.add(targetUserId);
        }
        if (role != null && !role.isBlank()) {
            sql.append(" AND (target_role = ? OR target_role IS NULL)");
            params.add(role.trim().toLowerCase());
        }
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            setParameters(statement, params);
            statement.executeUpdate();
        }
    }

    private void ensureNotificationTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS notifications ("
                + "id_notification INT AUTO_INCREMENT PRIMARY KEY, "
                + "tipe VARCHAR(50) NOT NULL, "
                + "judul VARCHAR(120) NOT NULL, "
                + "pesan VARCHAR(255) NOT NULL, "
                + "target_user_id INT NULL, "
                + "target_role VARCHAR(30) NULL, "
                + "is_read TINYINT(1) NOT NULL DEFAULT 0, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "INDEX idx_notifications_target (target_user_id, target_role, is_read))";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void setParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }
}
