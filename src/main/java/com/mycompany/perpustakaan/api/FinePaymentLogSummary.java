package com.mycompany.perpustakaan.api;

public class FinePaymentLogSummary {

    private final int idPeminjaman;
    private final String statusPembayaran;
    private final Integer actorUserId;
    private final String actorName;
    private final String note;
    private final String createdAt;

    public FinePaymentLogSummary(int idPeminjaman, String statusPembayaran, Integer actorUserId, String actorName, String note, String createdAt) {
        this.idPeminjaman = idPeminjaman;
        this.statusPembayaran = statusPembayaran;
        this.actorUserId = actorUserId;
        this.actorName = actorName;
        this.note = note;
        this.createdAt = createdAt;
    }

    public int getIdPeminjaman() { return idPeminjaman; }
    public String getStatusPembayaran() { return statusPembayaran; }
    public Integer getActorUserId() { return actorUserId; }
    public String getActorName() { return actorName; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }
}