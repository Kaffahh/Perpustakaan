package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.controller.AuthController;
import com.mycompany.perpustakaan.model.User;

public class TestAuthSession {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Gunakan: TestAuthSession <username> <password>");
            return;
        }

        String username = args[0];
        String password = args[1];

        AuthController authController = new AuthController();

        try {
            User user = authController.login(username, password);

            if (user == null) {
                System.out.println("Login gagal: username atau password salah.");
                return;
            }

            System.out.println("Login berhasil.");
            System.out.println("ID User: " + user.getIdUser());
            System.out.println("Nama: " + user.getNama());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Username: " + user.getUsername());
            System.out.println("Role: " + user.getRole());
            System.out.println("Created At: " + user.getCreatedAt());
            System.out.println("Session aktif: " + authController.isLoggedIn());
            System.out.println("Is Admin: " + authController.isAdmin());
            System.out.println("Is Staff: " + authController.isStaff());
            System.out.println("Is Anggota: " + authController.isAnggota());

            authController.logout();
            System.out.println("Session aktif setelah logout: " + authController.isLoggedIn());
        } catch (Exception exception) {
            System.out.println("Auth test gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }
}