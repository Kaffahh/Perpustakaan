package com.mycompany.perpustakaan.api;

public class BookRequest {

    private final String kodeBuku;
    private final String isbn;
    private final String judul;
    private final String penulis;
    private final String penerbit;
    private final String kategori;
    private final Integer tahunTerbit;
    private final int stokTersedia;
    private final int stokTotal;

    public BookRequest(String kodeBuku, String judul, String penulis, String penerbit, String kategori, Integer tahunTerbit, int stokTersedia, int stokTotal) {
        this(kodeBuku, null, judul, penulis, penerbit, kategori, tahunTerbit, stokTersedia, stokTotal);
    }

    public BookRequest(String kodeBuku, String isbn, String judul, String penulis, String penerbit, String kategori, Integer tahunTerbit, int stokTersedia, int stokTotal) {
        this.kodeBuku = kodeBuku;
        this.isbn = isbn;
        this.judul = judul;
        this.penulis = penulis;
        this.penerbit = penerbit;
        this.kategori = kategori;
        this.tahunTerbit = tahunTerbit;
        this.stokTersedia = stokTersedia;
        this.stokTotal = stokTotal;
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
}
