package com.mycompany.perpustakaan.model;

public class User {

    private final int idUser;
    private final String nama;
    private final String email;
    private final String username;
    private final String password;
    private final String role;
    private final String statusAkun;
    private final String createdAt;

    public User(int idUser, String nama, String email, String username, String password, String role, String createdAt) {
        this(idUser, nama, email, username, password, role, "aktif", createdAt);
    }

    public User(int idUser, String nama, String email, String username, String password, String role, String statusAkun, String createdAt) {
        this.idUser = idUser;
        this.nama = nama;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.statusAkun = statusAkun == null ? "aktif" : statusAkun;
        this.createdAt = createdAt;
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

    public String getPassword() {
        return password;
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

    public boolean isAdmin() {
        return hasRole("admin");
    }

    public boolean isStaff() {
        return hasRole("staff");
    }

    public boolean isAnggota() {
        return hasRole("anggota") || hasRole("member") || hasRole("user");
    }

    public boolean isSuspended() {
        return statusAkun != null && statusAkun.equalsIgnoreCase("suspend");
    }

    private boolean hasRole(String expectedRole) {
        return role != null && role.equalsIgnoreCase(expectedRole);
    }
}
