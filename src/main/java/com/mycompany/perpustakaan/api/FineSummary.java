package com.mycompany.perpustakaan.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FineSummary {

    private final int idPeminjaman;
    private final String namaUser;
    private final String username;
    private final String judulBuku;
    private final LocalDate tanggalJatuhTempo;
    private final LocalDate tanggalKembali;
    private final BigDecimal nominalDenda;
    private final String statusPembayaran;
    private final String updatedAt;

    public FineSummary(int idPeminjaman, String namaUser, String username, String judulBuku,
            LocalDate tanggalJatuhTempo, LocalDate tanggalKembali, BigDecimal nominalDenda,
            String statusPembayaran, String updatedAt) {
        this.idPeminjaman = idPeminjaman;
        this.namaUser = namaUser;
        this.username = username;
        this.judulBuku = judulBuku;
        this.tanggalJatuhTempo = tanggalJatuhTempo;
        this.tanggalKembali = tanggalKembali;
        this.nominalDenda = nominalDenda;
        this.statusPembayaran = statusPembayaran;
        this.updatedAt = updatedAt;
    }

    public int getIdPeminjaman() {
        return idPeminjaman;
    }

    public String getNamaUser() {
        return namaUser;
    }

    public String getUsername() {
        return username;
    }

    public String getJudulBuku() {
        return judulBuku;
    }

    public LocalDate getTanggalJatuhTempo() {
        return tanggalJatuhTempo;
    }

    public LocalDate getTanggalKembali() {
        return tanggalKembali;
    }

    public BigDecimal getNominalDenda() {
        return nominalDenda;
    }

    public String getStatusPembayaran() {
        return statusPembayaran;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
