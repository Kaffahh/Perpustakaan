package com.mycompany.perpustakaan.api;

public class PopularBookReportRow {

    private final int idBuku;
    private final String kodeBuku;
    private final String isbn;
    private final String judul;
    private final String penulis;
    private final String kategori;
    private final int totalDipinjam;
    private final int stokTersedia;
    private final int stokTotal;
    private final String statusKetersediaan;

    public PopularBookReportRow(int idBuku, String kodeBuku, String isbn, String judul, String penulis, String kategori, int totalDipinjam, int stokTersedia, int stokTotal, String statusKetersediaan) {
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.isbn = isbn;
        this.judul = judul;
        this.penulis = penulis;
        this.kategori = kategori;
        this.totalDipinjam = totalDipinjam;
        this.stokTersedia = stokTersedia;
        this.stokTotal = stokTotal;
        this.statusKetersediaan = statusKetersediaan;
    }

    public int getIdBuku() {
        return idBuku;
    }

    public String getKodeBuku() {
        return kodeBuku;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getJudul() {
        return judul;
    }

    public String getPenulis() {
        return penulis;
    }

    public String getKategori() {
        return kategori;
    }

    public int getTotalDipinjam() {
        return totalDipinjam;
    }

    public int getStokTersedia() {
        return stokTersedia;
    }

    public int getStokTotal() {
        return stokTotal;
    }

    public String getStatusKetersediaan() {
        return statusKetersediaan;
    }
}
