package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.MemberRequest;
import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    public User findByUsername(String username) throws SQLException {
        String sql = userSelectSql() + " WHERE username = ? LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapUser(resultSet);
            }
        }
    }

    public User findById(int idUser) throws SQLException {
        String sql = userSelectSql() + " WHERE id_user = ? LIMIT 1";

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

    public User insertMember(MemberRequest request, String hashedPassword) throws SQLException {
        String sql = "INSERT INTO users (username, nama_lengkap, email, password, role) VALUES (?, ?, ?, ?, 'anggota')";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, request.getUsername());
            statement.setString(2, request.getNama());
            statement.setString(3, request.getEmail());
            statement.setString(4, hashedPassword);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Gagal mengambil id anggota baru.");
                }
                return findById(generatedKeys.getInt(1));
            }
        }
    }

    public User updateMember(int idUser, MemberRequest request, String hashedPassword) throws SQLException {
        StringBuilder sql = new StringBuilder("UPDATE users SET username = ?, nama_lengkap = ?, email = ?");
        if (hashedPassword != null) {
            sql.append(", password = ?");
        }
        sql.append(" WHERE id_user = ? AND role = 'anggota'");

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            int index = 1;
            statement.setString(index++, request.getUsername());
            statement.setString(index++, request.getNama());
            statement.setString(index++, request.getEmail());
            if (hashedPassword != null) {
                statement.setString(index++, hashedPassword);
            }
            statement.setInt(index, idUser);

            if (statement.executeUpdate() == 0) {
                return null;
            }
            return findById(idUser);
        }
    }

    public User updateMemberStatus(int idUser, String statusAkun) throws SQLException {
        ensureStatusAkunColumn();
        String sql = "UPDATE users SET status_akun = ? WHERE id_user = ? AND role = 'anggota'";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statusAkun);
            statement.setInt(2, idUser);

            if (statement.executeUpdate() == 0) {
                return null;
            }
            return findById(idUser);
        }
    }

    public User updateProfile(int idUser, String nama, String email) throws SQLException {
        String sql = "UPDATE users SET nama_lengkap = ?, email = ? WHERE id_user = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, nama);
            statement.setString(2, email);
            statement.setInt(3, idUser);
            if (statement.executeUpdate() == 0) {
                return null;
            }
            return findById(idUser);
        }
    }

    public User updatePassword(int idUser, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id_user = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, hashedPassword);
            statement.setInt(2, idUser);
            if (statement.executeUpdate() == 0) {
                return null;
            }
            return findById(idUser);
        }
    }

    public boolean deleteMemberById(int idUser) throws SQLException {
        String sql = "DELETE FROM users WHERE id_user = ? AND role = 'anggota'";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);
            return statement.executeUpdate() > 0;
        }
    }

    public List<User> searchMembers(String keyword, String statusAkun, int limit, int offset) throws SQLException {
        ensureStatusColumnIfFiltering(statusAkun);
        StringBuilder sql = new StringBuilder(userSelectSql() + " WHERE role = 'anggota'");
        List<Object> parameters = new ArrayList<>();

        appendMemberFilters(sql, parameters, keyword, statusAkun);
        sql.append(" ORDER BY nama_lengkap ASC, id_user ASC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        List<User> users = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        }

        return users;
    }

    public int countMembers(String keyword, String statusAkun) throws SQLException {
        ensureStatusColumnIfFiltering(statusAkun);
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM users WHERE role = 'anggota'");
        List<Object> parameters = new ArrayList<>();

        appendMemberFilters(sql, parameters, keyword, statusAkun);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        }

        return 0;
    }

    public void ensureStatusAkunColumn() throws SQLException {
        if (hasStatusAkunColumn()) {
            return;
        }

        String sql = "ALTER TABLE users ADD COLUMN status_akun ENUM('aktif','suspend') NOT NULL DEFAULT 'aktif' AFTER role";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        int idUser = resultSet.getInt("id_user");
        String username = resultSet.getString("username");
        String nama = resultSet.getString("nama_lengkap");
        String email = resultSet.getString("email");
        String password = resultSet.getString("password");
        String role = resultSet.getString("role");
        String statusAkun = resultSet.getString("status_akun");
        String createdAt = resultSet.getString("created_at");

        return new User(idUser, nama, email, username, password, role, statusAkun, createdAt);
    }

    private String userSelectSql() throws SQLException {
        String statusExpression = hasStatusAkunColumn() ? "status_akun" : "'aktif' AS status_akun";
        return "SELECT id_user, username, nama_lengkap, email, password, role, " + statusExpression + ", created_at FROM users";
    }

    private boolean hasStatusAkunColumn() throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'status_akun' LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            return resultSet.next();
        }
    }

    private void ensureStatusColumnIfFiltering(String statusAkun) throws SQLException {
        if (statusAkun != null) {
            ensureStatusAkunColumn();
        }
    }

    private void appendMemberFilters(StringBuilder sql, List<Object> parameters, String keyword, String statusAkun) {
        if (keyword != null) {
            String pattern = "%" + keyword + "%";
            sql.append(" AND (username LIKE ? OR nama_lengkap LIKE ? OR email LIKE ?)");
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }

        if (statusAkun != null) {
            sql.append(" AND status_akun = ?");
            parameters.add(statusAkun);
        }
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }
}
