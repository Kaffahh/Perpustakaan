package com.mycompany.perpustakaan.api;

public class NotificationSummary {

    private final String tipe;
    private final String judul;
    private final String pesan;
    private final String createdAt;

    public NotificationSummary(String tipe, String judul, String pesan, String createdAt) {
        this.tipe = tipe;
        this.judul = judul;
        this.pesan = pesan;
        this.createdAt = createdAt;
    }

    public String getTipe() {
        return tipe;
    }

    public String getJudul() {
        return judul;
    }

    public String getPesan() {
        return pesan;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
