package com.mycompany.perpustakaan.api;

import java.math.BigDecimal;

public class AdminDashboardSummary {

    private final int totalBuku;
    private final int totalAnggota;
    private final int totalPeminjamanAktif;
    private final BigDecimal totalDenda;

    public AdminDashboardSummary(int totalBuku, int totalAnggota, int totalPeminjamanAktif, BigDecimal totalDenda) {
        this.totalBuku = totalBuku;
        this.totalAnggota = totalAnggota;
        this.totalPeminjamanAktif = totalPeminjamanAktif;
        this.totalDenda = totalDenda;
    }

    public int getTotalBuku() {
        return totalBuku;
    }

    public int getTotalAnggota() {
        return totalAnggota;
    }

    public int getTotalPeminjamanAktif() {
        return totalPeminjamanAktif;
    }

    public BigDecimal getTotalDenda() {
        return totalDenda;
    }
}
