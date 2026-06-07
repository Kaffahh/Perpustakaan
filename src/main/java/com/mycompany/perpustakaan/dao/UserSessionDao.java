package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.PersistentSession;
import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class UserSessionDao {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DB_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_SESSION_DAYS = 14;

    public PersistentSession createSession(User user) throws SQLException {
        return createSession(user, DEFAULT_SESSION_DAYS);
    }

    public PersistentSession createSession(User user, int days) throws SQLException {
        ensureSessionTable();
        if (user == null) {
            throw new IllegalArgumentException("User session wajib memiliki user.");
        }

        String token = generateToken();
        String tokenHash = hashToken(token);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(days < 1 ? DEFAULT_SESSION_DAYS : days);
        String expiresAtText = DB_DATE_TIME.format(expiresAt);

        String sql = "INSERT INTO user_sessions (id_user, token_hash, expires_at) VALUES (?, ?, ?)";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, user.getIdUser());
            statement.setString(2, tokenHash);
            statement.setString(3, expiresAtText);
            statement.executeUpdate();
        }

        return new PersistentSession(user, token, expiresAtText);
    }

    public User restoreSession(String token) throws SQLException {
        ensureSessionTable();
        if (token == null || token.isBlank()) {
            return null;
        }

        String sql = "SELECT u.id_user, u.username, u.nama_lengkap, u.email, u.password, u.role, "
                + userStatusExpression() + ", u.created_at "
                + "FROM user_sessions s "
                + "JOIN users u ON u.id_user = s.id_user "
                + "WHERE s.token_hash = ? AND s.revoked_at IS NULL AND s.expires_at > NOW() "
                + "LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, hashToken(token));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                User user = mapUser(resultSet);
                if (user.isSuspended()) {
                    revokeSession(token);
                    return null;
                }
                return user;
            }
        }
    }

    public void revokeSession(String token) throws SQLException {
        ensureSessionTable();
        if (token == null || token.isBlank()) {
            return;
        }

        String sql = "UPDATE user_sessions SET revoked_at = NOW() WHERE token_hash = ? AND revoked_at IS NULL";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, hashToken(token));
            statement.executeUpdate();
        }
    }

    public void revokeAllUserSessions(int idUser) throws SQLException {
        ensureSessionTable();
        String sql = "UPDATE user_sessions SET revoked_at = NOW() WHERE id_user = ? AND revoked_at IS NULL";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);
            statement.executeUpdate();
        }
    }

    public void cleanupExpiredSessions() throws SQLException {
        ensureSessionTable();
        String sql = "DELETE FROM user_sessions WHERE expires_at < DATE_SUB(NOW(), INTERVAL 30 DAY) OR revoked_at < DATE_SUB(NOW(), INTERVAL 30 DAY)";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private void ensureSessionTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS user_sessions ("
                + "id_session INT AUTO_INCREMENT PRIMARY KEY, "
                + "id_user INT NOT NULL, "
                + "token_hash CHAR(64) NOT NULL UNIQUE, "
                + "expires_at DATETIME NOT NULL, "
                + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                + "revoked_at DATETIME NULL, "
                + "INDEX idx_user_sessions_user (id_user), "
                + "INDEX idx_user_sessions_token (token_hash), "
                + "CONSTRAINT fk_user_sessions_user FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE CASCADE)";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 tidak tersedia.", exception);
        }
    }

    private String userStatusExpression() throws SQLException {
        return hasStatusAkunColumn() ? "u.status_akun" : "'aktif' AS status_akun";
    }

    private boolean hasStatusAkunColumn() throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'status_akun' LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id_user"),
                resultSet.getString("nama_lengkap"),
                resultSet.getString("email"),
                resultSet.getString("username"),
                resultSet.getString("password"),
                resultSet.getString("role"),
                resultSet.getString("status_akun"),
                resultSet.getString("created_at"));
    }
}
