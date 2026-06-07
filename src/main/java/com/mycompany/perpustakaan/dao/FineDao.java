package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.FineSummary;
import com.mycompany.perpustakaan.config.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FineDao {

    public List<FineSummary> findFines(String keyword, String status, int limit, int offset) throws SQLException {
        ensureFinePaymentTable();
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_peminjaman, u.nama_lengkap, u.username, b.judul, p.tanggal_jatuh_tempo, "
                + "p.tanggal_kembali, p.denda, fp.status_pembayaran, fp.updated_at "
                + "FROM peminjaman p "
                + "JOIN users u ON u.id_user = p.id_user "
                + "JOIN buku b ON b.id_buku = p.id_buku "
                + "LEFT JOIN fine_payments fp ON fp.id_peminjaman = p.id_peminjaman "
                + "WHERE p.denda > 0");
        List<Object> params = new ArrayList<>();

        appendFineFilters(sql, params, keyword, status);
        sql.append(" ORDER BY p.tanggal_kembali DESC, p.id_peminjaman DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<FineSummary> fines = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    fines.add(mapFine(resultSet));
                }
            }
        }
        return fines;
    }

    public int countFines(String keyword, String status) throws SQLException {
        ensureFinePaymentTable();
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS total FROM peminjaman p "
                + "JOIN users u ON u.id_user = p.id_user "
                + "JOIN buku b ON b.id_buku = p.id_buku "
                + "LEFT JOIN fine_payments fp ON fp.id_peminjaman = p.id_peminjaman "
                + "WHERE p.denda > 0");
        List<Object> params = new ArrayList<>();
        appendFineFilters(sql, params, keyword, status);

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, params);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        }
        return 0;
    }

    public void markFineStatus(int idPeminjaman, String statusPembayaran) throws SQLException {
        ensureFinePaymentTable();
        String status = normalizePaymentStatus(statusPembayaran);
        String sql = "INSERT INTO fine_payments (id_peminjaman, status_pembayaran, paid_at) "
                + "VALUES (?, ?, CASE WHEN ? = 'paid' THEN NOW() ELSE NULL END) "
                + "ON DUPLICATE KEY UPDATE status_pembayaran = VALUES(status_pembayaran), "
                + "paid_at = VALUES(paid_at), updated_at = CURRENT_TIMESTAMP";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idPeminjaman);
            statement.setString(2, status);
            statement.setString(3, status);
            statement.executeUpdate();
        }
    }

    public BigDecimal sumUnpaidFines() throws SQLException {
        ensureFinePaymentTable();
        String sql = "SELECT COALESCE(SUM(p.denda), 0) AS total FROM peminjaman p "
                + "LEFT JOIN fine_payments fp ON fp.id_peminjaman = p.id_peminjaman "
                + "WHERE p.denda > 0 AND COALESCE(fp.status_pembayaran, 'unpaid') = 'unpaid'";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    private void ensureFinePaymentTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS fine_payments ("
                + "id_peminjaman INT NOT NULL PRIMARY KEY, "
                + "status_pembayaran ENUM('unpaid','paid','waived') NOT NULL DEFAULT 'unpaid', "
                + "paid_at DATETIME NULL, "
                + "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, "
                + "CONSTRAINT fk_fine_payments_peminjaman FOREIGN KEY (id_peminjaman) "
                + "REFERENCES peminjaman(id_peminjaman) ON DELETE CASCADE)";
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    private void appendFineFilters(StringBuilder sql, List<Object> params, String keyword, String status) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (u.nama_lengkap LIKE ? OR u.username LIKE ? OR b.judul LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
        }
        String normalizedStatus = normalizeNullableStatus(status);
        if (normalizedStatus != null) {
            sql.append(" AND COALESCE(fp.status_pembayaran, 'unpaid') = ?");
            params.add(normalizedStatus);
        }
    }

    private FineSummary mapFine(ResultSet resultSet) throws SQLException {
        Date jatuhTempo = resultSet.getDate("tanggal_jatuh_tempo");
        Date kembali = resultSet.getDate("tanggal_kembali");
        String status = resultSet.getString("status_pembayaran");
        return new FineSummary(
                resultSet.getInt("id_peminjaman"),
                resultSet.getString("nama_lengkap"),
                resultSet.getString("username"),
                resultSet.getString("judul"),
                jatuhTempo == null ? null : jatuhTempo.toLocalDate(),
                kembali == null ? null : kembali.toLocalDate(),
                resultSet.getBigDecimal("denda"),
                status == null ? "unpaid" : status,
                resultSet.getString("updated_at"));
    }

    private String normalizePaymentStatus(String status) {
        String normalized = normalizeNullableStatus(status);
        if (normalized == null) {
            throw new IllegalArgumentException("Status pembayaran denda wajib diisi.");
        }
        return normalized;
    }

    private String normalizeNullableStatus(String status) {
        if (status == null || status.isBlank() || "semua".equalsIgnoreCase(status) || "all".equalsIgnoreCase(status)) {
            return null;
        }
        String normalized = status.trim().toLowerCase();
        if (!"unpaid".equals(normalized) && !"paid".equals(normalized) && !"waived".equals(normalized)) {
            throw new IllegalArgumentException("Status pembayaran denda tidak valid.");
        }
        return normalized;
    }

    private void setParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }
}
