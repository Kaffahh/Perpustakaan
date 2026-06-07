package com.mycompany.perpustakaan.api;

public class CategorySummary {

    private final String namaKategori;
    private final int totalBuku;
    private final int stokTersedia;
    private final int stokTotal;

    public CategorySummary(String namaKategori, int totalBuku, int stokTersedia, int stokTotal) {
        this.namaKategori = namaKategori;
        this.totalBuku = totalBuku;
        this.stokTersedia = stokTersedia;
        this.stokTotal = stokTotal;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public int getTotalBuku() {
        return totalBuku;
    }

    public int getStokTersedia() {
        return stokTersedia;
    }

    public int getStokTotal() {
        return stokTotal;
    }
}
