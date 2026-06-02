package com.mycompany.perpustakaan.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanReportRow {

    private final int idPeminjaman;
    private final int idUser;
    private final String namaUser;
    private final String username;
    private final int idBuku;
    private final String kodeBuku;
    private final String judulBuku;
    private final LocalDate tanggalPinjam;
    private final LocalDate tanggalJatuhTempo;
    private final LocalDate tanggalKembali;
    private final String status;
    private final BigDecimal denda;

    public LoanReportRow(int idPeminjaman, int idUser, String namaUser, String username, int idBuku, String kodeBuku, String judulBuku, LocalDate tanggalPinjam, LocalDate tanggalJatuhTempo, LocalDate tanggalKembali, String status, BigDecimal denda) {
        this.idPeminjaman = idPeminjaman;
        this.idUser = idUser;
        this.namaUser = namaUser;
        this.username = username;
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.judulBuku = judulBuku;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalJatuhTempo = tanggalJatuhTempo;
        this.tanggalKembali = tanggalKembali;
        this.status = status;
        this.denda = denda;
    }

    public int getIdPeminjaman() {
        return idPeminjaman;
    }

    public int getIdUser() {
        return idUser;
    }

    public String getNamaUser() {
        return namaUser;
    }

    public String getUsername() {
        return username;
    }

    public int getIdBuku() {
        return idBuku;
    }

    public String getKodeBuku() {
        return kodeBuku;
    }

    public String getJudulBuku() {
        return judulBuku;
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
}
