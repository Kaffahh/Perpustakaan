package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.InventoryReportRow;
import com.mycompany.perpustakaan.api.LoanReportRow;
import com.mycompany.perpustakaan.api.PopularBookReportRow;
import com.mycompany.perpustakaan.config.Database;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    public int countBooks() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM buku";
        return count(sql);
    }

    public int countMembers() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM users WHERE role IN ('anggota', 'member', 'user')";
        return count(sql);
    }

    public int countActiveLoans() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM peminjaman WHERE tanggal_kembali IS NULL AND status IN ('dipinjam', 'terlambat')";
        return count(sql);
    }

    public BigDecimal sumFines() throws SQLException {
        String sql = "SELECT COALESCE(SUM(denda), 0) AS total FROM peminjaman";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    public List<InventoryReportRow> getInventoryReport() throws SQLException {
        boolean hasIsbn = hasIsbnColumn();
        String sql = "SELECT id_buku, kode_buku, " + isbnExpression(hasIsbn) + ", judul, penulis, penerbit, kategori, tahun_terbit, stok_tersedia, stok_total FROM buku ORDER BY judul ASC, id_buku ASC";
        List<InventoryReportRow> rows = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rows.add(mapInventoryRow(resultSet));
            }
        }
        return rows;
    }

    public List<PopularBookReportRow> getPopularBookReport(int limit) throws SQLException {
        boolean hasIsbn = hasIsbnColumn();
        String sql = "SELECT b.id_buku, b.kode_buku, " + isbnExpression("b", hasIsbn)
                + ", b.judul, b.penulis, b.kategori, COUNT(p.id_peminjaman) AS total_dipinjam, "
                + "b.stok_tersedia, b.stok_total "
                + "FROM buku b "
                + "LEFT JOIN peminjaman p ON p.id_buku = b.id_buku "
                + "GROUP BY b.id_buku, b.kode_buku, " + groupByIsbn("b", hasIsbn)
                + "b.judul, b.penulis, b.kategori, b.stok_tersedia, b.stok_total "
                + "ORDER BY total_dipinjam DESC, b.judul ASC, b.id_buku ASC LIMIT ?";
        List<PopularBookReportRow> rows = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Math.max(1, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(mapPopularBookRow(resultSet));
                }
            }
        }
        return rows;
    }

    public List<LoanReportRow> getLoanReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id_peminjaman, p.id_user, u.nama_lengkap, u.username, p.id_buku, b.kode_buku, b.judul, "
                + "p.tanggal_pinjam, p.tanggal_jatuh_tempo, p.tanggal_kembali, p.status, p.denda "
                + "FROM peminjaman p "
                + "JOIN users u ON u.id_user = p.id_user "
                + "JOIN buku b ON b.id_buku = p.id_buku "
                + "WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        if (startDate != null) {
            sql.append(" AND p.tanggal_pinjam >= ?");
            parameters.add(Date.valueOf(startDate));
        }
        if (endDate != null) {
            sql.append(" AND p.tanggal_pinjam <= ?");
            parameters.add(Date.valueOf(endDate));
        }
        sql.append(" ORDER BY p.tanggal_pinjam DESC, p.id_peminjaman DESC");

        List<LoanReportRow> rows = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(mapLoanRow(resultSet));
                }
            }
        }
        return rows;
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        }
        return 0;
    }

    private InventoryReportRow mapInventoryRow(ResultSet resultSet) throws SQLException {
        Integer tahunTerbit = resultSet.getObject("tahun_terbit") != null ? resultSet.getInt("tahun_terbit") : null;
        return new InventoryReportRow(
                resultSet.getInt("id_buku"),
                resultSet.getString("kode_buku"),
                resultSet.getString("isbn"),
                resultSet.getString("judul"),
                resultSet.getString("penulis"),
                resultSet.getString("penerbit"),
                resultSet.getString("kategori"),
                tahunTerbit,
                resultSet.getInt("stok_tersedia"),
                resultSet.getInt("stok_total"),
                statusKetersediaan(resultSet.getInt("stok_tersedia")));
    }

    private PopularBookReportRow mapPopularBookRow(ResultSet resultSet) throws SQLException {
        int stokTersedia = resultSet.getInt("stok_tersedia");
        return new PopularBookReportRow(
                resultSet.getInt("id_buku"),
                resultSet.getString("kode_buku"),
                resultSet.getString("isbn"),
                resultSet.getString("judul"),
                resultSet.getString("penulis"),
                resultSet.getString("kategori"),
                resultSet.getInt("total_dipinjam"),
                stokTersedia,
                resultSet.getInt("stok_total"),
                statusKetersediaan(stokTersedia));
    }

    private LoanReportRow mapLoanRow(ResultSet resultSet) throws SQLException {
        Date tanggalKembali = resultSet.getDate("tanggal_kembali");
        BigDecimal denda = resultSet.getBigDecimal("denda");
        return new LoanReportRow(
                resultSet.getInt("id_peminjaman"),
                resultSet.getInt("id_user"),
                resultSet.getString("nama_lengkap"),
                resultSet.getString("username"),
                resultSet.getInt("id_buku"),
                resultSet.getString("kode_buku"),
                resultSet.getString("judul"),
                resultSet.getDate("tanggal_pinjam").toLocalDate(),
                resultSet.getDate("tanggal_jatuh_tempo").toLocalDate(),
                tanggalKembali == null ? null : tanggalKembali.toLocalDate(),
                resultSet.getString("status"),
                denda == null ? BigDecimal.ZERO : denda);
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }

    private String statusKetersediaan(int stokTersedia) {
        return stokTersedia > 0 ? "Tersedia" : "Dipinjam";
    }

    private String isbnExpression(boolean hasIsbn) {
        return isbnExpression("", hasIsbn);
    }

    private String isbnExpression(String tableAlias, boolean hasIsbn) {
        String prefix = tableAlias == null || tableAlias.isBlank() ? "" : tableAlias + ".";
        return hasIsbn ? prefix + "isbn" : "NULL AS isbn";
    }

    private String groupByIsbn(String tableAlias, boolean hasIsbn) {
        if (!hasIsbn) {
            return "";
        }
        String prefix = tableAlias == null || tableAlias.isBlank() ? "" : tableAlias + ".";
        return prefix + "isbn, ";
    }

    private boolean hasIsbnColumn() throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'buku' AND column_name = 'isbn' LIMIT 1";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next();
        }
    }
}
