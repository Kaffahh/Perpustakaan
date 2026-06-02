package com.mycompany.perpustakaan.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Peminjaman {

    private final int idPeminjaman;
    private final int idUser;
    private final int idBuku;
    private final LocalDate tanggalPinjam;
    private final LocalDate tanggalJatuhTempo;
    private final LocalDate tanggalKembali;
    private final String status;
    private final BigDecimal denda;
    private final Integer createdBy;
    private final String kodeBuku;
    private final String judulBuku;
    private final String penulisBuku;
    private final String kategoriBuku;

    public Peminjaman(int idPeminjaman, int idUser, int idBuku, LocalDate tanggalPinjam, LocalDate tanggalJatuhTempo, LocalDate tanggalKembali, String status, BigDecimal denda, Integer createdBy, String kodeBuku, String judulBuku, String penulisBuku, String kategoriBuku) {
        this.idPeminjaman = idPeminjaman;
        this.idUser = idUser;
        this.idBuku = idBuku;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalJatuhTempo = tanggalJatuhTempo;
        this.tanggalKembali = tanggalKembali;
        this.status = status;
        this.denda = denda;
        this.createdBy = createdBy;
        this.kodeBuku = kodeBuku;
        this.judulBuku = judulBuku;
        this.penulisBuku = penulisBuku;
        this.kategoriBuku = kategoriBuku;
    }

    public int getIdPeminjaman() {
        return idPeminjaman;
    }

    public int getIdUser() {
        return idUser;
    }

    public int getIdBuku() {
        return idBuku;
    }

    public LocalDate getTanggalPinjam() {
        return tanggalPinjam;
    }

    public LocalDate getTanggalJatuhTempo() {
        return tanggalJatuhTempo;
    }

    public LocalDate getTanggalKembali() {
        return tanggalKembali;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getDenda() {
        return denda;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public String getKodeBuku() {
        return kodeBuku;
    }

    public String getJudulBuku() {
        return judulBuku;
    }

    public String getPenulisBuku() {
        return penulisBuku;
    }

    public String getKategoriBuku() {
        return kategoriBuku;
    }
}
