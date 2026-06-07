package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.Kunjungan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class KunjunganDao {

    public Kunjungan createVisit(Integer idUser, String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        String sql = "INSERT INTO kunjungan (id_user, nama_pengunjung, jenis_pengunjung, asal_instansi, keperluan, status_kunjungan) VALUES (?, ?, ?, ?, ?, 'datang')";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (idUser == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, idUser);
            }
            statement.setString(2, namaPengunjung);
            statement.setString(3, jenisPengunjung);
            statement.setString(4, asalInstansi);
            statement.setString(5, keperluan);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Gagal mengambil id kunjungan baru.");
                }
                return findById(generatedKeys.getInt(1));
            }
        }
    }

    public Kunjungan createManualVisit(String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        return createVisit(null, namaPengunjung, jenisPengunjung, asalInstansi, keperluan);
    }

    public List<Kunjungan> findRecentVisits(int limit) throws SQLException {
        return searchVisits(null, null, limit, 0);
    }

    public List<Kunjungan> searchVisits(String keyword, String status, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id_kunjungan, id_user, nama_pengunjung, jenis_pengunjung, asal_instansi, keperluan, status_kunjungan, tanggal_kunjungan "
                + "FROM kunjungan WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendVisitFilters(sql, params, keyword, status);
        sql.append(" ORDER BY tanggal_kunjungan DESC, id_kunjungan DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Kunjungan> visits = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, params);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    visits.add(mapKunjungan(resultSet));
                }
            }
        }
        return visits;
    }

    public int countVisits(String keyword, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM kunjungan WHERE 1=1");
        List<Object> params = new ArrayList<>();
        appendVisitFilters(sql, params, keyword, status);

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

    public Kunjungan updateStatus(int idKunjungan, String statusKunjungan) throws SQLException {
        String sql = "UPDATE kunjungan SET status_kunjungan = ? WHERE id_kunjungan = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, statusKunjungan);
            statement.setInt(2, idKunjungan);
            int updatedRows = statement.executeUpdate();

            if (updatedRows == 0) {
                return null;
            }
            return findById(idKunjungan);
        }
    }

    public Kunjungan findById(int idKunjungan) throws SQLException {
        String sql = "SELECT id_kunjungan, id_user, nama_pengunjung, jenis_pengunjung, asal_instansi, keperluan, status_kunjungan, tanggal_kunjungan FROM kunjungan WHERE id_kunjungan = ? LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idKunjungan);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapKunjungan(resultSet);
            }
        }
    }

    private Kunjungan mapKunjungan(ResultSet resultSet) throws SQLException {
        Integer idUser = resultSet.getObject("id_user") != null ? resultSet.getInt("id_user") : null;
        return new Kunjungan(
                resultSet.getInt("id_kunjungan"),
                idUser,
                resultSet.getString("nama_pengunjung"),
                resultSet.getString("jenis_pengunjung"),
                resultSet.getString("asal_instansi"),
                resultSet.getString("keperluan"),
                resultSet.getString("status_kunjungan"),
                resultSet.getString("tanggal_kunjungan"));
    }

    private void appendVisitFilters(StringBuilder sql, List<Object> params, String keyword, String status) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (nama_pengunjung LIKE ? OR jenis_pengunjung LIKE ? OR asal_instansi LIKE ? OR keperluan LIKE ?)");
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        String normalizedStatus = normalizeStatus(status);
        if (normalizedStatus != null) {
            sql.append(" AND status_kunjungan = ?");
            params.add(normalizedStatus);
        }
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank() || "semua".equalsIgnoreCase(status) || "all".equalsIgnoreCase(status)) {
            return null;
        }
        return status.trim().toLowerCase();
    }

    private void setParameters(PreparedStatement statement, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            statement.setObject(i + 1, params.get(i));
        }
    }
}
