package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.api.CategorySummary;
import com.mycompany.perpustakaan.config.Database;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    public List<CategorySummary> findCategorySummaries() throws SQLException {
        String sql = "SELECT COALESCE(NULLIF(TRIM(kategori), ''), 'Tanpa Kategori') AS nama_kategori, "
                + "COUNT(*) AS total_buku, COALESCE(SUM(stok_tersedia), 0) AS stok_tersedia, "
                + "COALESCE(SUM(stok_total), 0) AS stok_total "
                + "FROM buku GROUP BY COALESCE(NULLIF(TRIM(kategori), ''), 'Tanpa Kategori') "
                + "ORDER BY nama_kategori ASC";
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
        String sql = "UPDATE buku SET kategori = ? WHERE kategori = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, requireCategory(newName, "Kategori baru wajib diisi."));
            statement.setString(2, requireCategory(oldName, "Kategori lama wajib diisi."));
            return statement.executeUpdate();
        }
    }

    public int clearCategory(String categoryName) throws SQLException {
        String sql = "UPDATE buku SET kategori = NULL WHERE kategori = ?";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, requireCategory(categoryName, "Kategori wajib diisi."));
            return statement.executeUpdate();
        }
    }

    private String requireCategory(String value, String message) {
        if (value == null || value.trim().isEmpty() || "Tanpa Kategori".equalsIgnoreCase(value.trim())) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
