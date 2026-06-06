package com.mycompany.perpustakaan.api;

import com.mycompany.perpustakaan.model.Peminjaman;
import java.math.BigDecimal;
import java.time.LocalDate;

public class LoanSummary {

    private final int idPeminjaman;
    private final int idBuku;
    private final String kodeBuku;
    private final String judulBuku;
    private final String penulisBuku;
    private final String kategoriBuku;
    private final LocalDate tanggalPinjam;
    private final LocalDate tanggalJatuhTempo;
    private final LocalDate tanggalKembali;
    private final String status;
    private final int hariTerlambat;
    private final BigDecimal dendaBerjalan;
    private final String namaUser;
    private final String usernameUser;

    public LoanSummary(int idPeminjaman, int idBuku, String kodeBuku, String judulBuku, String penulisBuku, String kategoriBuku, LocalDate tanggalPinjam, LocalDate tanggalJatuhTempo, LocalDate tanggalKembali, String status, int hariTerlambat, BigDecimal dendaBerjalan, String namaUser, String usernameUser) {
        this.idPeminjaman = idPeminjaman;
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.judulBuku = judulBuku;
        this.penulisBuku = penulisBuku;
        this.kategoriBuku = kategoriBuku;
        this.tanggalPinjam = tanggalPinjam;
        this.tanggalJatuhTempo = tanggalJatuhTempo;
        this.tanggalKembali = tanggalKembali;
        this.status = status;
        this.hariTerlambat = hariTerlambat;
        this.dendaBerjalan = dendaBerjalan;
        this.namaUser = namaUser;
        this.usernameUser = usernameUser;
    }

    public static LoanSummary from(Peminjaman peminjaman, int hariTerlambat, BigDecimal dendaBerjalan) {
        if (peminjaman == null) {
            return null;
        }

        String displayStatus = peminjaman.getStatus();
        if ("dipinjam".equalsIgnoreCase(displayStatus) && hariTerlambat > 0) {
            displayStatus = "terlambat";
        }

        return new LoanSummary(
                peminjaman.getIdPeminjaman(),
                peminjaman.getIdBuku(),
                peminjaman.getKodeBuku(),
                peminjaman.getJudulBuku(),
                peminjaman.getPenulisBuku(),
                peminjaman.getKategoriBuku(),
                peminjaman.getTanggalPinjam(),
                peminjaman.getTanggalJatuhTempo(),
                peminjaman.getTanggalKembali(),
                displayStatus,
                hariTerlambat,
                dendaBerjalan,
                peminjaman.getNamaUser(),
                peminjaman.getUsernameUser());
    }

    public int getIdPeminjaman() {
        return idPeminjaman;
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

    public String getPenulisBuku() {
        return penulisBuku;
    }

    public String getKategoriBuku() {
        return kategoriBuku;
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

    public int getHariTerlambat() {
        return hariTerlambat;
    }

    public BigDecimal getDendaBerjalan() {
        return dendaBerjalan;
    }

    public String getNamaUser() {
        return namaUser;
    }

    public String getUsernameUser() {
        return usernameUser;
    }
}
