package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.CategorySummary;
import com.mycompany.perpustakaan.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public List<CategorySummary> findCategorySummaries() throws SQLException {
        ensureCategorySchema();
        String sql = "SELECT c.nama_kategori, COUNT(b.id_buku) AS total_buku, "
                + "COALESCE(SUM(b.stok_tersedia), 0) AS stok_tersedia, "
                + "COALESCE(SUM(b.stok_total), 0) AS stok_total "
                + "FROM categories c "
                + "LEFT JOIN buku b ON b.category_id = c.id_category "
                + "GROUP BY c.id_category, c.nama_kategori "
                + "ORDER BY c.nama_kategori ASC";
        List<CategorySummary> categories = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                categories.add(new CategorySummary(
                        resultSet.getString("nama_kategori"),
                        resultSet.getInt("total_buku"),
                        resultSet.getInt("stok_tersedia"),
                        resultSet.getInt("stok_total")));
            }
        }
        return categories;
    }

    public int renameCategory(String oldName, String newName) throws SQLException {
        ensureCategorySchema();
        String safeOld = requireCategory(oldName, "Kategori lama wajib diisi.");
        String safeNew = requireCategory(newName, "Kategori baru wajib diisi.");

        try (Connection connection = Database.getConnection()) {
            boolean previousAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                Integer existingId = findCategoryId(connection, safeNew);
                Integer oldId = findCategoryId(connection, safeOld);
                if (oldId == null) {
                    connection.rollback();
                    return 0;
                }

                int updatedBooks;
                if (existingId != null && !existingId.equals(oldId)) {
                    updatedBooks = moveBooksToCategory(connection, oldId, existingId, safeNew);
                    deleteCategoryById(connection, oldId);
                } else {
                    updateCategoryName(connection, oldId, safeNew);
                    updatedBooks = updateBookCategoryText(connection, oldId, safeNew);
                }
                connection.commit();
                return updatedBooks;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    public int clearCategory(String categoryName) throws SQLException {
        ensureCategorySchema();
        String safeCategory = requireCategory(categoryName, "Kategori wajib diisi.");
        Integer categoryId;
        try (Connection connection = Database.getConnection()) {
            categoryId = findCategoryId(connection, safeCategory);
        }
        if (categoryId == null) {
            return 0;
        }
        String sql = "UPDATE buku SET kategori = NULL, category_id = NULL WHERE category_id = ? OR kategori = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, categoryId);
            statement.setString(2, safeCategory);
            return statement.executeUpdate();
        }
    }

    public CategorySummary createCategory(String categoryName) throws SQLException {
        ensureCategorySchema();
        String safeCategory = requireCategory(categoryName, "Nama kategori wajib diisi.");
        try (Connection connection = Database.getConnection()) {
            int id = getOrCreateCategoryId(connection, safeCategory);
            return findCategorySummaryById(connection, id);
        }
    }

    public void ensureCategorySchema() throws SQLException {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS categories ("
                    + "id_category INT AUTO_INCREMENT PRIMARY KEY, "
                    + "nama_kategori VARCHAR(100) NOT NULL UNIQUE, "
                    + "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            if (!hasColumn(connection, "buku", "category_id")) {
                statement.executeUpdate("ALTER TABLE buku ADD COLUMN category_id INT NULL AFTER kategori");
            }
            syncCategories(connection);
        }
    }

    public int getOrCreateCategoryId(Connection connection, String categoryName) throws SQLException {
        String safeCategory = requireCategory(categoryName, "Nama kategori wajib diisi.");
        Integer existing = findCategoryId(connection, safeCategory);
        if (existing != null) {
            return existing;
        }
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO categories (nama_kategori) VALUES (?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, safeCategory);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Gagal membuat kategori baru.");
    }

    private void syncCategories(Connection connection) throws SQLException {
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT IGNORE INTO categories (nama_kategori) "
                + "SELECT DISTINCT TRIM(kategori) FROM buku WHERE kategori IS NOT NULL AND TRIM(kategori) <> ''")) {
            insert.executeUpdate();
        }
        try (PreparedStatement update = connection.prepareStatement(
                "UPDATE buku b JOIN categories c ON c.nama_kategori = b.kategori "
                + "SET b.category_id = c.id_category "
                + "WHERE b.category_id IS NULL AND b.kategori IS NOT NULL AND b.kategori <> ''")) {
            update.executeUpdate();
        }
    }

    private CategorySummary findCategorySummaryById(Connection connection, int idCategory) throws SQLException {
        String sql = "SELECT c.nama_kategori, COUNT(b.id_buku) AS total_buku, "
                + "COALESCE(SUM(b.stok_tersedia), 0) AS stok_tersedia, COALESCE(SUM(b.stok_total), 0) AS stok_total "
                + "FROM categories c LEFT JOIN buku b ON b.category_id = c.id_category "
                + "WHERE c.id_category = ? GROUP BY c.id_category, c.nama_kategori";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idCategory);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new CategorySummary(
                            resultSet.getString("nama_kategori"),
                            resultSet.getInt("total_buku"),
                            resultSet.getInt("stok_tersedia"),
                            resultSet.getInt("stok_total"));
                }
            }
        }
        return new CategorySummary("", 0, 0, 0);
    }

    private Integer findCategoryId(Connection connection, String categoryName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT id_category FROM categories WHERE nama_kategori = ? LIMIT 1")) {
            statement.setString(1, categoryName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id_category");
                }
            }
        }
        return null;
    }

    private int moveBooksToCategory(Connection connection, int oldId, int newId, String newName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE buku SET category_id = ?, kategori = ? WHERE category_id = ?")) {
            statement.setInt(1, newId);
            statement.setString(2, newName);
            statement.setInt(3, oldId);
            return statement.executeUpdate();
        }
    }

    private void updateCategoryName(Connection connection, int idCategory, String newName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE categories SET nama_kategori = ? WHERE id_category = ?")) {
            statement.setString(1, newName);
            statement.setInt(2, idCategory);
            statement.executeUpdate();
        }
    }

    private int updateBookCategoryText(Connection connection, int idCategory, String newName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE buku SET kategori = ? WHERE category_id = ?")) {
            statement.setString(1, newName);
            statement.setInt(2, idCategory);
            return statement.executeUpdate();
        }
    }

    private void deleteCategoryById(Connection connection, int idCategory) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM categories WHERE id_category = ?")) {
            statement.setInt(1, idCategory);
            statement.executeUpdate();
        }
    }

    private boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ? LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String requireCategory(String value, String message) {
        if (value == null || value.trim().isEmpty() || "Tanpa Kategori".equalsIgnoreCase(value.trim())) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
