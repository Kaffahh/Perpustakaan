package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.BookRequest;
import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.Buku;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BukuDao {

    private final CategoryDao categoryDao = new CategoryDao();

    public int countAll() throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM buku";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
            return 0;
        }
    }

    public List<Buku> findLatest(int limit) throws SQLException {
        String sql = "SELECT " + selectBookColumns() + " FROM buku ORDER BY created_at DESC, id_buku DESC LIMIT ?";
        List<Buku> books = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapBuku(resultSet));
                }
            }
        }

        return books;
    }

    public List<Buku> findPopular(int limit) throws SQLException {
        boolean hasIsbn = hasIsbnColumn();
        String sql = "SELECT " + selectBookColumns("b", hasIsbn)
                + " FROM buku b "
                + "LEFT JOIN peminjaman p ON p.id_buku = b.id_buku "
                + "GROUP BY " + groupByBookColumns("b", hasIsbn)
                + " ORDER BY COUNT(p.id_peminjaman) DESC, b.created_at DESC, b.id_buku DESC LIMIT ?";
        List<Buku> books = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, Math.max(1, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapBuku(resultSet));
                }
            }
        }

        return books;
    }

    public List<Buku> search(String keyword, int limit, int offset) throws SQLException {
        boolean hasIsbn = hasIsbnColumn();
        String isbnFilter = hasIsbn ? " OR isbn LIKE ?" : "";
        String sql = "SELECT " + selectBookColumns(hasIsbn) + " FROM buku WHERE judul LIKE ? OR penulis LIKE ? OR kategori LIKE ?" + isbnFilter + " ORDER BY judul ASC, id_buku ASC LIMIT ? OFFSET ?";
        List<Buku> books = new ArrayList<>();
        String pattern = "%" + keyword + "%";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            int parameterIndex = 4;
            if (hasIsbn) {
                statement.setString(parameterIndex++, pattern);
            }
            statement.setInt(parameterIndex++, limit);
            statement.setInt(parameterIndex, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapBuku(resultSet));
                }
            }
        }

        return books;
    }

    public List<Buku> findBooks(String keyword, String kategori, int limit, int offset) throws SQLException {
        boolean hasIsbn = hasIsbnColumn();
        StringBuilder sql = new StringBuilder("SELECT " + selectBookColumns(hasIsbn) + " FROM buku WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        appendBookshelfFilters(sql, parameters, keyword, kategori, hasIsbn);
        sql.append(" ORDER BY judul ASC, id_buku ASC LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        List<Buku> books = new ArrayList<>();
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {

            setParameters(statement, parameters);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapBuku(resultSet));
                }
            }
        }

        return books;
    }

    public int countBooks(String keyword, String kategori) throws SQLException {
        boolean hasIsbn = hasIsbnColumn();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) AS total FROM buku WHERE 1=1");
        List<Object> parameters = new ArrayList<>();

        appendBookshelfFilters(sql, parameters, keyword, kategori, hasIsbn);

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

    public List<String> findCategories() throws SQLException {
        String sql = "SELECT DISTINCT kategori FROM buku WHERE kategori IS NOT NULL AND kategori <> '' ORDER BY kategori ASC";
        List<String> categories = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(resultSet.getString("kategori"));
            }
        }

        return categories;
    }

    public Buku findById(int idBuku) throws SQLException {
        String sql = "SELECT " + selectBookColumns() + " FROM buku WHERE id_buku = ? LIMIT 1";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idBuku);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                return mapBuku(resultSet);
            }
        }
    }

    public Buku insert(BookRequest request, int createdBy) throws SQLException {
        categoryDao.ensureCategorySchema();
        String sql = "INSERT INTO buku (kode_buku, judul, penulis, penerbit, kategori, category_id, tahun_terbit, stok_tersedia, stok_total, created_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            fillBookStatement(connection, statement, request);
            statement.setInt(10, createdBy);
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (!generatedKeys.next()) {
                    throw new SQLException("Gagal mengambil id buku baru.");
                }
                return findById(generatedKeys.getInt(1));
            }
        }
    }

    public Buku update(int idBuku, BookRequest request) throws SQLException {
        categoryDao.ensureCategorySchema();
        String sql = "UPDATE buku SET kode_buku = ?, judul = ?, penulis = ?, penerbit = ?, kategori = ?, category_id = ?, tahun_terbit = ?, stok_tersedia = ?, stok_total = ? WHERE id_buku = ?";

        try (Connection connection = Database.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            fillBookStatement(connection, statement, request);
            statement.setInt(10, idBuku);
            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                return null;
            }
            return findById(idBuku);
        }
    }

    public Buku updateStock(int idBuku, int stokTersedia, int stokTotal) throws SQLException {
        String sql = "UPDATE buku SET stok_tersedia = ?, stok_total = ? WHERE id_buku = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, stokTersedia);
            statement.setInt(2, stokTotal);
            statement.setInt(3, idBuku);
            int updatedRows = statement.executeUpdate();
            if (updatedRows == 0) {
                return null;
            }
            return findById(idBuku);
        }
    }

    public boolean deleteById(int idBuku) throws SQLException {
        String sql = "DELETE FROM buku WHERE id_buku = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idBuku);
            return statement.executeUpdate() > 0;
        }
    }

    private void fillBookStatement(Connection connection, PreparedStatement statement, BookRequest request) throws SQLException {
        statement.setString(1, request.getKodeBuku());
        statement.setString(2, request.getJudul());
        statement.setString(3, request.getPenulis());
        statement.setString(4, request.getPenerbit());
        statement.setString(5, request.getKategori());
        if (request.getKategori() == null || request.getKategori().trim().isEmpty()) {
            statement.setNull(6, java.sql.Types.INTEGER);
        } else {
            statement.setInt(6, categoryDao.getOrCreateCategoryId(connection, request.getKategori()));
        }
        if (request.getTahunTerbit() == null) {
            statement.setNull(7, java.sql.Types.INTEGER);
        } else {
            statement.setInt(7, request.getTahunTerbit());
        }
        statement.setInt(8, request.getStokTersedia());
        statement.setInt(9, request.getStokTotal());
    }

    private void appendBookshelfFilters(StringBuilder sql, List<Object> parameters, String keyword, String kategori, boolean hasIsbn) {
        if (keyword != null) {
            String pattern = "%" + keyword + "%";
            sql.append(" AND (judul LIKE ? OR penulis LIKE ? OR penerbit LIKE ? OR kategori LIKE ? OR kode_buku LIKE ?");
            if (hasIsbn) {
                sql.append(" OR isbn LIKE ?");
            }
            sql.append(")");
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            if (hasIsbn) {
                parameters.add(pattern);
            }
        }

        if (kategori != null) {
            sql.append(" AND kategori = ?");
            parameters.add(kategori);
        }
    }

    private void setParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }

    private Buku mapBuku(ResultSet resultSet) throws SQLException {
        int idBuku = resultSet.getInt("id_buku");
        String kodeBuku = resultSet.getString("kode_buku");
        String isbn = resultSet.getString("isbn");
        String judul = resultSet.getString("judul");
        String penulis = resultSet.getString("penulis");
        String penerbit = resultSet.getString("penerbit");
        String kategori = resultSet.getString("kategori");
        Integer tahunTerbit = resultSet.getObject("tahun_terbit") != null ? resultSet.getInt("tahun_terbit") : null;
        int stokTersedia = resultSet.getInt("stok_tersedia");
        int stokTotal = resultSet.getInt("stok_total");
        Integer createdBy = resultSet.getObject("created_by") != null ? resultSet.getInt("created_by") : null;
        String createdAt = resultSet.getString("created_at");

        return new Buku(idBuku, kodeBuku, isbn, judul, penulis, penerbit, kategori, tahunTerbit, stokTersedia, stokTotal, createdBy, createdAt);
    }

    private String selectBookColumns() throws SQLException {
        return selectBookColumns(hasIsbnColumn());
    }

    private String selectBookColumns(boolean hasIsbn) {
        String isbnExpression = hasIsbn ? "isbn" : "NULL AS isbn";
        return "id_buku, kode_buku, " + isbnExpression + ", judul, penulis, penerbit, kategori, tahun_terbit, stok_tersedia, stok_total, created_by, created_at";
    }

    private String selectBookColumns(String tableAlias, boolean hasIsbn) {
        String prefix = tableAlias == null || tableAlias.isBlank() ? "" : tableAlias + ".";
        String isbnExpression = hasIsbn ? prefix + "isbn" : "NULL AS isbn";
        return prefix + "id_buku, " + prefix + "kode_buku, " + isbnExpression + ", "
                + prefix + "judul, " + prefix + "penulis, " + prefix + "penerbit, "
                + prefix + "kategori, " + prefix + "tahun_terbit, " + prefix + "stok_tersedia, "
                + prefix + "stok_total, " + prefix + "created_by, " + prefix + "created_at";
    }

    private String groupByBookColumns(String tableAlias, boolean hasIsbn) {
        String prefix = tableAlias == null || tableAlias.isBlank() ? "" : tableAlias + ".";
        String isbnColumn = hasIsbn ? prefix + "isbn, " : "";
        return prefix + "id_buku, " + prefix + "kode_buku, " + isbnColumn
                + prefix + "judul, " + prefix + "penulis, " + prefix + "penerbit, "
                + prefix + "kategori, " + prefix + "tahun_terbit, " + prefix + "stok_tersedia, "
                + prefix + "stok_total, " + prefix + "created_by, " + prefix + "created_at";
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
