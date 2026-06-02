package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.config.Database;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDatabaseConnection {

    public static void main(String[] args) {
        try (Connection connection = Database.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {

            if (resultSet.next()) {
                System.out.println("Koneksi database berhasil.");
                System.out.println("Hasil query uji: " + resultSet.getInt(1));
            } else {
                System.out.println("Koneksi berhasil, tetapi query uji tidak mengembalikan data.");
            }
        } catch (Exception exception) {
            System.out.println("Koneksi database gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }
}