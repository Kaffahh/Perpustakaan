package com.mycompany.perpustakaan.model;

public class Buku {

    private final int idBuku;
    private final String kodeBuku;
    private final String judul;
    private final String penulis;
    private final String penerbit;
    private final String kategori;
    private final Integer tahunTerbit;
    private final int stokTersedia;
    private final int stokTotal;
    private final Integer createdBy;
    private final String createdAt;

    public Buku(int idBuku, String kodeBuku, String judul, String penulis, String penerbit, String kategori, Integer tahunTerbit, int stokTersedia, int stokTotal, Integer createdBy, String createdAt) {
        this.idBuku = idBuku;
        this.kodeBuku = kodeBuku;
        this.judul = judul;
        this.penulis = penulis;
        this.penerbit = penerbit;
        this.kategori = kategori;
        this.tahunTerbit = tahunTerbit;
        this.stokTersedia = stokTersedia;
        this.stokTotal = stokTotal;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public int getIdBuku() {
        return idBuku;
    }

    public String getKodeBuku() {
        return kodeBuku;
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

    public Integer getCreatedBy() {
        return createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 971d3bf3dfadeb3f2eb35438307861006fcebde6
