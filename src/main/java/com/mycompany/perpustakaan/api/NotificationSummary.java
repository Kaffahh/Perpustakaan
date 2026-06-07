package com.mycompany.perpustakaan.api;

public class NotificationSummary {

    private final String tipe;
    private final String judul;
    private final String pesan;
    private final String createdAt;
    private final boolean isRead;

    public NotificationSummary(String tipe, String judul, String pesan, String createdAt) {
        this(tipe, judul, pesan, createdAt, false);
    }

    public NotificationSummary(String tipe, String judul, String pesan, String createdAt, boolean isRead) {
        this.tipe = tipe;
        this.judul = judul;
        this.pesan = pesan;
        this.createdAt = createdAt;
        this.isRead = isRead;
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

    public boolean isRead() {
        return isRead;
    }
}
