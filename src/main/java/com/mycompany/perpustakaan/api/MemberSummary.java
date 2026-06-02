package com.mycompany.perpustakaan.api;

import com.mycompany.perpustakaan.model.User;

public class MemberSummary {

    private final int idUser;
    private final String nama;
    private final String email;
    private final String username;
    private final String role;
    private final String statusAkun;
    private final String createdAt;

    public MemberSummary(int idUser, String nama, String email, String username, String role, String statusAkun, String createdAt) {
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.username = username;
        this.role = role;
        this.statusAkun = statusAkun;
        this.createdAt = createdAt;
    }

    public static MemberSummary from(User user) {
        if (user == null) {
            return null;
        }
        return new MemberSummary(user.getIdUser(), user.getNama(), user.getEmail(), user.getUsername(), user.getRole(), user.getStatusAkun(), user.getCreatedAt());
    }

    public int getIdUser() {
        return idUser;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public String getStatusAkun() {
        return statusAkun;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
