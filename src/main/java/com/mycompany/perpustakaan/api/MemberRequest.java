package com.mycompany.perpustakaan.api;

public class MemberRequest {

    private final String username;
    private final String nama;
    private final String email;
    private final String password;

    public MemberRequest(String username, String nama, String email, String password) {
        this.username = username;
        this.nama = nama;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
