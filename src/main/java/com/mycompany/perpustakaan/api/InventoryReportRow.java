package com.mycompany.perpustakaan.api;

public class InventoryReportRow {

    private final int idBuku;
    private final String kodeBuku;
    private final String isbn;
    private final String judul;
    private final String penulis;
    private final String penerbit;
    private final String kategori;
    private final Integer tahunTerbit;
    private final int stokTersedia;
    private final int stokTotal;
    private final String statusKetersediaan;

    public InventoryReportRow(int idBuku, String kodeBuku, String judul, String penulis, String penerbit, String kategori, Integer tahunTerbit, int stokTersedia, int stokTotal) {
        this(idBuku, kodeBuku, null, judul, penulis, penerbit, kategori, tahunTerbit, stokTersedia, stokTotal, stokTersedia > 0 ? "Tersedia" : "Dipinjam");
    }

    public InventoryReportRow(int idBuku, String kodeBuku, String isbn, String judul, String penulis, String penerbit, String kategori, Integer tahunTerbit, int stokTersedia, int stokTotal, String statusKetersediaan) {
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.isbn = isbn;
        this.judul = judul;
        this.penulis = penulis;
        this.penerbit = penerbit;
        this.kategori = kategori;
        this.tahunTerbit = tahunTerbit;
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

    public String getPenerbit() {
        return penerbit;
    }

    public String getKategori() {
        return kategori;
    }

    public Integer getTahunTerbit() {
        return tahunTerbit;
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
