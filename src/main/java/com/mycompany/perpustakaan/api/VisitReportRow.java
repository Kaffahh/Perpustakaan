package com.mycompany.perpustakaan.api;

public class VisitReportRow {

    private final int idKunjungan;
    private final String namaPengunjung;
    private final String jenisPengunjung;
    private final String asalInstansi;
    private final String keperluan;
    private final String statusKunjungan;
    private final String tanggalKunjungan;

    public VisitReportRow(int idKunjungan, String namaPengunjung, String jenisPengunjung,
            String asalInstansi, String keperluan, String statusKunjungan, String tanggalKunjungan) {
        this.idKunjungan = idKunjungan;
        this.namaPengunjung = namaPengunjung;
        this.jenisPengunjung = jenisPengunjung;
        this.asalInstansi = asalInstansi;
        this.keperluan = keperluan;
        this.statusKunjungan = statusKunjungan;
        this.tanggalKunjungan = tanggalKunjungan;
    }

    public int getIdKunjungan() {
        return idKunjungan;
    }

    public String getNamaPengunjung() {
        return namaPengunjung;
    }

    public String getJenisPengunjung() {
        return jenisPengunjung;
    }

    public String getAsalInstansi() {
        return asalInstansi;
    }

    public String getKeperluan() {
        return keperluan;
    }

    public String getStatusKunjungan() {
        return statusKunjungan;
    }

    public String getTanggalKunjungan() {
        return tanggalKunjungan;
    }
}
