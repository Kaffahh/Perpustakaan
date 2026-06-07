package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.Peminjaman;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PeminjamanDao {

    public boolean hasActiveLoanForBook(int idUser, int idBuku) throws SQLException {
        String sql = "SELECT 1 FROM peminjaman WHERE id_user = ? AND id_buku = ? AND tanggal_kembali IS NULL AND status IN ('menunggu', 'dipinjam', 'terlambat') LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);
            statement.setInt(2, idBuku);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int countActiveLoansByUser(int idUser) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM peminjaman WHERE id_user = ? AND tanggal_kembali IS NULL AND status IN ('menunggu', 'dipinjam', 'terlambat')";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        }

        return 0;
    }

    public int countLoansByBook(int idBuku) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM peminjaman WHERE id_buku = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idBuku);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        }

        return 0;
    }

    public int countLoansByUser(int idUser) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM peminjaman WHERE id_user = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
            }
        }

        return 0;
    }

    public Peminjaman createLoan(int idUser, int idBuku, LocalDate tanggalPinjam, LocalDate tanggalJatuhTempo, Integer createdBy) throws SQLException {
        String selectBookSql = "SELECT stok_tersedia FROM buku WHERE id_buku = ? FOR UPDATE";
        String insertLoanSql = "INSERT INTO peminjaman (id_user, id_buku, tanggal_pinjam, tanggal_jatuh_tempo, status, denda, created_by) VALUES (?, ?, ?, ?, 'dipinjam', 0.00, ?)";
        String updateStockSql = "UPDATE buku SET stok_tersedia = stok_tersedia - 1 WHERE id_buku = ? AND stok_tersedia > 0";

        try (Connection connection = Database.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int stock = getLockedBookStock(connection, selectBookSql, idBuku);
                if (stock <= 0) {
                    connection.rollback();
                    return null;
                }

                int idPeminjaman = insertLoan(connection, insertLoanSql, idUser, idBuku, tanggalPinjam, tanggalJatuhTempo, createdBy);
                int updatedRows = updateStock(connection, updateStockSql, idBuku);
                if (updatedRows != 1) {
                    connection.rollback();
                    return null;
                }

                connection.commit();
                return findById(idPeminjaman);
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public Peminjaman createPendingLoan(int idUser, int idBuku, LocalDate tanggalPinjam, LocalDate tanggalJatuhTempo) throws SQLException {
        String sql = "INSERT INTO peminjaman (id_user, id_buku, tanggal_pinjam, tanggal_jatuh_tempo, status, denda) VALUES (?, ?, ?, ?, 'menunggu', 0.00)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, idUser);
            statement.setInt(2, idBuku);
            statement.setDate(3, Date.valueOf(tanggalPinjam));
            statement.setDate(4, Date.valueOf(tanggalJatuhTempo));
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Gagal mengambil id peminjaman baru.");
                }
                return findById(generatedKeys.getInt(1));
            }
        }
    }

    public Peminjaman approveLoan(int idPeminjaman) throws SQLException {
        String selectLoanSql = "SELECT id_buku, status FROM peminjaman WHERE id_peminjaman = ? FOR UPDATE";
        String updateStatusSql = "UPDATE peminjaman SET status = 'dipinjam', tanggal_pinjam = ? WHERE id_peminjaman = ? AND status = 'menunggu'";
        String updateStockSql = "UPDATE buku SET stok_tersedia = stok_tersedia - 1 WHERE id_buku = ? AND stok_tersedia > 0";

        try (Connection connection = Database.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int idBuku;
                try (PreparedStatement statement = connection.prepareStatement(selectLoanSql)) {
                    statement.setInt(1, idPeminjaman);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Data peminjaman tidak ditemukan.");
                        }
                        if (!"menunggu".equalsIgnoreCase(resultSet.getString("status"))) {
                            throw new SQLException("Pinjaman tidak dalam status menunggu persetujuan.");
                        }
                        idBuku = resultSet.getInt("id_buku");
                    }
                }

                int stock;
                try (PreparedStatement statement = connection.prepareStatement("SELECT stok_tersedia FROM buku WHERE id_buku = ? FOR UPDATE")) {
                    statement.setInt(1, idBuku);
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Buku tidak ditemukan.");
                        }
                        stock = resultSet.getInt("stok_tersedia");
                    }
                }

                if (stock <= 0) {
                    connection.rollback();
                    throw new SQLException("Stok buku tidak tersedia.");
                }

                try (PreparedStatement statement = connection.prepareStatement(updateStatusSql)) {
                    statement.setDate(1, Date.valueOf(LocalDate.now()));
                    statement.setInt(2, idPeminjaman);
                    int updated = statement.executeUpdate();
                    if (updated != 1) {
                        connection.rollback();
                        throw new SQLException("Gagal menyetujui peminjaman.");
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(updateStockSql)) {
                    statement.setInt(1, idBuku);
                    statement.executeUpdate();
                }

                connection.commit();
                return findById(idPeminjaman);
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public Peminjaman rejectLoan(int idPeminjaman) throws SQLException {
        String sql = "UPDATE peminjaman SET status = 'ditolak' WHERE id_peminjaman = ? AND status = 'menunggu'";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idPeminjaman);
            int updated = statement.executeUpdate();
            if (updated != 1) {
                throw new SQLException("Gagal menolak peminjaman atau status bukan menunggu.");
            }
            return findById(idPeminjaman);
        }
    }

    public List<Peminjaman> findPendingLoans() throws SQLException {
        return findPendingLoans(null, 5000, 0);
    }

    public List<Peminjaman> findPendingLoans(String keyword, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT p.id_peminjaman, p.id_user, p.id_buku, p.tanggal_pinjam, p.tanggal_jatuh_tempo, p.tanggal_kembali, p.status, p.denda, p.created_by, b.kode_buku, b.judul, b.penulis, b.kategori, u.nama_lengkap AS nama_user, u.username FROM peminjaman p JOIN buku b ON b.id_buku = p.id_buku JOIN users u ON u.id_user = p.id_user WHERE p.status = 'menunggu'");
        List<Object> parameters = new ArrayList<>();

        appendPendingKeywordFilter(sql, parameters, keyword);
        sql.append(" ORDER BY p.tanggal_pinjam ASC, p.id_peminjaman ASC LIMIT ? OFFSET ?");
        parameters.add(Math.max(1, limit));
        parameters.add(Math.max(0, offset));

        List<Peminjaman> loans = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(mapPeminjamanWithUser(resultSet));
                }
            }
        }
        return loans;
    }

    public int countPendingLoans(String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM peminjaman p JOIN buku b ON b.id_buku = p.id_buku JOIN users u ON u.id_user = p.id_user WHERE p.status = 'menunggu'");
        List<Object> parameters = new ArrayList<>();
        appendPendingKeywordFilter(sql, parameters, keyword);

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

    private Peminjaman mapPeminjamanWithUser(ResultSet resultSet) throws SQLException {
        Date tanggalKembali = resultSet.getDate("tanggal_kembali");
        BigDecimal denda = resultSet.getBigDecimal("denda");
        Integer createdBy = resultSet.getObject("created_by") != null ? resultSet.getInt("created_by") : null;

        Peminjaman p = new Peminjaman(
                resultSet.getInt("id_peminjaman"),
                resultSet.getInt("id_user"),
                resultSet.getInt("id_buku"),
                resultSet.getDate("tanggal_pinjam").toLocalDate(),
                resultSet.getDate("tanggal_jatuh_tempo").toLocalDate(),
                tanggalKembali == null ? null : tanggalKembali.toLocalDate(),
                resultSet.getString("status"),
                denda == null ? BigDecimal.ZERO : denda,
                createdBy,
                resultSet.getString("kode_buku"),
                resultSet.getString("judul"),
                resultSet.getString("penulis"),
                resultSet.getString("kategori"));
        // Store user info in a thread-safe way or use a separate DTO
        p.setNamaUser(resultSet.getString("nama_user"));
        p.setUsernameUser(resultSet.getString("username"));
        return p;
    }

    public List<Peminjaman> findActiveLoansByUser(int idUser) throws SQLException {
        String sql = "SELECT p.id_peminjaman, p.id_user, p.id_buku, p.tanggal_pinjam, p.tanggal_jatuh_tempo, p.tanggal_kembali, p.status, p.denda, p.created_by, b.kode_buku, b.judul, b.penulis, b.kategori FROM peminjaman p JOIN buku b ON b.id_buku = p.id_buku WHERE p.id_user = ? AND p.tanggal_kembali IS NULL AND p.status IN ('dipinjam', 'terlambat', 'menunggu') ORDER BY p.tanggal_pinjam DESC, p.id_peminjaman ASC";
        List<Peminjaman> loans = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idUser);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(mapPeminjaman(resultSet));
                }
            }
        }

        return loans;
    }

    public Peminjaman findById(int idPeminjaman) throws SQLException {
        String sql = "SELECT p.id_peminjaman, p.id_user, p.id_buku, p.tanggal_pinjam, p.tanggal_jatuh_tempo, p.tanggal_kembali, p.status, p.denda, p.created_by, b.kode_buku, b.judul, b.penulis, b.kategori FROM peminjaman p JOIN buku b ON b.id_buku = p.id_buku WHERE p.id_peminjaman = ? LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idPeminjaman);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapPeminjaman(resultSet);
            }
        }
    }

    public List<Peminjaman> findLoansForManagement(String status, int limit, int offset) throws SQLException {
        return findLoansForManagement(status, null, limit, offset);
    }

    public List<Peminjaman> findLoansForManagement(String status, String keyword, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT p.id_peminjaman, p.id_user, p.id_buku, p.tanggal_pinjam, p.tanggal_jatuh_tempo, p.tanggal_kembali, p.status, p.denda, p.created_by, b.kode_buku, b.judul, b.penulis, b.kategori FROM peminjaman p JOIN buku b ON b.id_buku = p.id_buku JOIN users u ON u.id_user = p.id_user WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        appendManagementStatusFilter(sql, parameters, status);
        appendManagementKeywordFilter(sql, parameters, keyword);
        sql.append(" ORDER BY p.tanggal_pinjam DESC, p.id_peminjaman DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        List<Peminjaman> loans = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(mapPeminjaman(resultSet));
                }
            }
        }

        return loans;
    }

    public int countLoansForManagement(String status) throws SQLException {
        return countLoansForManagement(status, null);
    }

    public int countLoansForManagement(String status, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM peminjaman p JOIN buku b ON b.id_buku = p.id_buku JOIN users u ON u.id_user = p.id_user WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        appendManagementStatusFilter(sql, parameters, status);
        appendManagementKeywordFilter(sql, parameters, keyword);

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

    public Peminjaman processReturn(int idPeminjaman, LocalDate tanggalKembali, String status, BigDecimal denda) throws SQLException {
        String selectLoanSql = "SELECT id_buku, tanggal_kembali, status FROM peminjaman WHERE id_peminjaman = ? FOR UPDATE";
        String updateLoanSql = "UPDATE peminjaman SET tanggal_kembali = ?, status = ?, denda = ? WHERE id_peminjaman = ?";
        String updateStockSql = "UPDATE buku SET stok_tersedia = LEAST(stok_tersedia + 1, stok_total) WHERE id_buku = ?";

        try (Connection connection = Database.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                int idBuku = getLockedLoanBookId(connection, selectLoanSql, idPeminjaman);
                updateReturnedLoan(connection, updateLoanSql, idPeminjaman, tanggalKembali, status, denda);
                updateReturnedBookStock(connection, updateStockSql, idBuku);

                connection.commit();
                return findById(idPeminjaman);
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public List<Peminjaman> findLoanHistoryByUser(int idUser, String status, int limit, int offset) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT p.id_peminjaman, p.id_user, p.id_buku, p.tanggal_pinjam, p.tanggal_jatuh_tempo, p.tanggal_kembali, p.status, p.denda, p.created_by, b.kode_buku, b.judul, b.penulis, b.kategori FROM peminjaman p JOIN buku b ON b.id_buku = p.id_buku WHERE p.id_user = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(idUser);

        appendHistoryStatusFilter(sql, parameters, status);
        sql.append(" ORDER BY p.tanggal_pinjam DESC, p.id_peminjaman DESC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        List<Peminjaman> loans = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    loans.add(mapPeminjaman(resultSet));
                }
            }
        }

        return loans;
    }

    public int countLoanHistoryByUser(int idUser, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM peminjaman p WHERE p.id_user = ?");
        List<Object> parameters = new ArrayList<>();
        parameters.add(idUser);

        appendHistoryStatusFilter(sql, parameters, status);

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

    private void appendHistoryStatusFilter(StringBuilder sql, List<Object> parameters, String status) {
        if (status != null) {
            sql.append(" AND p.status = ?");
            parameters.add(status);
        }
    }

    private void appendManagementStatusFilter(StringBuilder sql, List<Object> parameters, String status) {
        if (status != null) {
            if ("aktif".equalsIgnoreCase(status)) {
                sql.append(" AND p.tanggal_kembali IS NULL AND p.status IN ('dipinjam', 'terlambat')");
            } else {
                sql.append(" AND p.status = ?");
                parameters.add(status);
            }
        }
    }

    private void appendManagementKeywordFilter(StringBuilder sql, List<Object> parameters, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (b.judul LIKE ? OR b.penulis LIKE ? OR b.kode_buku LIKE ? OR u.nama_lengkap LIKE ? OR u.username LIKE ?)");
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
        }
    }

    private void appendPendingKeywordFilter(StringBuilder sql, List<Object> parameters, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            String like = "%" + keyword.trim() + "%";
            sql.append(" AND (b.judul LIKE ? OR b.penulis LIKE ? OR b.kode_buku LIKE ? OR b.kategori LIKE ? OR u.nama_lengkap LIKE ? OR u.username LIKE ?)");
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
            parameters.add(like);
        }
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }

    private int getLockedBookStock(Connection connection, String sql, int idBuku) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idBuku);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Buku tidak ditemukan.");
                }
                return resultSet.getInt("stok_tersedia");
            }
        }
    }

    private int insertLoan(Connection connection, String sql, int idUser, int idBuku, LocalDate tanggalPinjam, LocalDate tanggalJatuhTempo, Integer createdBy) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idUser);
            statement.setInt(2, idBuku);
            statement.setDate(3, Date.valueOf(tanggalPinjam));
            statement.setDate(4, Date.valueOf(tanggalJatuhTempo));
            if (createdBy == null) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(5, createdBy);
            }

            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Gagal mengambil id peminjaman baru.");
                }
                return generatedKeys.getInt(1);
            }
        }
    }

    private int updateStock(Connection connection, String sql, int idBuku) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idBuku);
            return statement.executeUpdate();
        }
    }

    private int getLockedLoanBookId(Connection connection, String sql, int idPeminjaman) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idPeminjaman);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Data peminjaman tidak ditemukan.");
                }
                if (resultSet.getDate("tanggal_kembali") != null || "dikembalikan".equalsIgnoreCase(resultSet.getString("status"))) {
                    throw new SQLException("Peminjaman sudah dikembalikan.");
                }
                return resultSet.getInt("id_buku");
            }
        }
    }

    private void updateReturnedLoan(Connection connection, String sql, int idPeminjaman, LocalDate tanggalKembali, String status, BigDecimal denda) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(tanggalKembali));
            statement.setString(2, status);
            statement.setBigDecimal(3, denda);
            statement.setInt(4, idPeminjaman);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Gagal memperbarui data peminjaman.");
            }
        }
    }

    private void updateReturnedBookStock(Connection connection, String sql, int idBuku) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idBuku);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Gagal memperbarui stok buku.");
            }
        }
    }

    private Peminjaman mapPeminjaman(ResultSet resultSet) throws SQLException {
        Date tanggalKembali = resultSet.getDate("tanggal_kembali");
        BigDecimal denda = resultSet.getBigDecimal("denda");
        Integer createdBy = resultSet.getObject("created_by") != null ? resultSet.getInt("created_by") : null;

        return new Peminjaman(
                resultSet.getInt("id_peminjaman"),
                resultSet.getInt("id_user"),
                resultSet.getInt("id_buku"),
                resultSet.getDate("tanggal_pinjam").toLocalDate(),
                resultSet.getDate("tanggal_jatuh_tempo").toLocalDate(),
                tanggalKembali == null ? null : tanggalKembali.toLocalDate(),
                resultSet.getString("status"),
                denda == null ? BigDecimal.ZERO : denda,
                createdBy,
                resultSet.getString("kode_buku"),
                resultSet.getString("judul"),
                resultSet.getString("penulis"),
                resultSet.getString("kategori"));
    }
}
