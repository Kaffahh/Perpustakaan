package com.mycompany.perpustakaan.api;

public class ProfileRequest {

    private final String nama;
    private final String email;

    public ProfileRequest(String nama, String email) {
        this.nama = nama;
        this.email = email;
    }

    public String getNama() {
        return nama;
    }

    public String getEmail() {
        return email;
    }
}
