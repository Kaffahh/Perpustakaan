package com.mycompany.perpustakaan.dao;

import com.mycompany.perpustakaan.config.Database;
import com.mycompany.perpustakaan.model.Buku;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BukuDao {

    public int countAll() throws SQLException {
<<<<<<< HEAD
        String sql = "SELECT COUNT(*) AS total FROM buku";
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getInt("total");
=======
        String sql = "SELECT COUNT(*) FROM buku";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
>>>>>>> 971d3bf3dfadeb3f2eb35438307861006fcebde6
            }
            return 0;
        }
    }

    public List<Buku> findLatest(int limit) throws SQLException {
<<<<<<< HEAD
        String sql = "SELECT id_buku, kode_buku, judul, penulis, penerbit, kategori, tahun_terbit, stok_tersedia, stok_total, created_by, created_at FROM buku ORDER BY created_at DESC, id_buku DESC LIMIT ?";
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

    public List<Buku> search(String keyword, int limit, int offset) throws SQLException {
        String sql = "SELECT id_buku, kode_buku, judul, penulis, penerbit, kategori, tahun_terbit, stok_tersedia, stok_total, created_by, created_at FROM buku WHERE judul LIKE ? OR penulis LIKE ? OR kategori LIKE ? ORDER BY judul ASC, id_buku ASC LIMIT ? OFFSET ?";
        List<Buku> books = new ArrayList<>();
        String pattern = "%" + keyword + "%";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);
            statement.setInt(4, limit);
            statement.setInt(5, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    books.add(mapBuku(resultSet));
                }
            }
        }

        return books;
    }

    private Buku mapBuku(ResultSet resultSet) throws SQLException {
        int idBuku = resultSet.getInt("id_buku");
        String kodeBuku = resultSet.getString("kode_buku");
        String judul = resultSet.getString("judul");
        String penulis = resultSet.getString("penulis");
        String penerbit = resultSet.getString("penerbit");
        String kategori = resultSet.getString("kategori");
        Integer tahunTerbit = resultSet.getObject("tahun_terbit") != null ? resultSet.getInt("tahun_terbit") : null;
        int stokTersedia = resultSet.getInt("stok_tersedia");
        int stokTotal = resultSet.getInt("stok_total");
        Integer createdBy = resultSet.getObject("created_by") != null ? resultSet.getInt("created_by") : null;
        String createdAt = resultSet.getString("created_at");

        return new Buku(idBuku, kodeBuku, judul, penulis, penerbit, kategori, tahunTerbit, stokTersedia, stokTotal, createdBy, createdAt);
    }
}
=======
        String sql = "SELECT id_buku, kode_buku, judul, penulis, penerbit, kategori, tahun_terbit, stok_tersedia, stok_total, created_by, created_at FROM buku ORDER BY created_at DESC LIMIT ?";
        List<Buku> list = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBuku(rs));
                }
            }
        }
        return list;
    }

    public List<Buku> search(String keyword, int limit, int offset) throws SQLException {
        String sql = "SELECT id_buku, kode_buku, judul, penulis, penerbit, kategori, tahun_terbit, stok_tersedia, stok_total, created_by, created_at FROM buku WHERE judul LIKE ? OR penulis LIKE ? OR kategori LIKE ? ORDER BY judul LIMIT ? OFFSET ?";
        List<Buku> list = new ArrayList<>();

        String pattern = "%" + keyword + "%";
        try (Connection connection = Database.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setInt(4, limit);
            stmt.setInt(5, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBuku(rs));
                }
            }
        }
        return list;
    }

    private Buku mapBuku(ResultSet rs) throws SQLException {
        int idBuku = rs.getInt("id_buku");
        String kode = rs.getString("kode_buku");
        String judul = rs.getString("judul");
        String penulis = rs.getString("penulis");
        String penerbit = rs.getString("penerbit");
        String kategori = rs.getString("kategori");
        Integer tahun = rs.getObject("tahun_terbit") != null ? rs.getInt("tahun_terbit") : null;
        int stokTersedia = rs.getInt("stok_tersedia");
        int stokTotal = rs.getInt("stok_total");
        Integer createdBy = rs.getObject("created_by") != null ? rs.getInt("created_by") : null;
        String createdAt = rs.getString("created_at");

        return new Buku(idBuku, kode, judul, penulis, penerbit, kategori, tahun, stokTersedia, stokTotal, createdBy, createdAt);
    }
}
>>>>>>> 971d3bf3dfadeb3f2eb35438307861006fcebde6
