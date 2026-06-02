package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.Kunjungan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
}
