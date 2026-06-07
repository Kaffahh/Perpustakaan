package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.dao.KunjunganDao;
import com.mycompany.perpustakaan.model.Kunjungan;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;
import java.util.List;

public class VisitController {

    private final KunjunganDao kunjunganDao;

    public VisitController() {
        this.kunjunganDao = new KunjunganDao();
    }

    public Kunjungan addRegisteredUserVisit(String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        User currentUser = requireLoggedInUser();
        String safeJenis = normalizeJenisPengunjung(jenisPengunjung);
        String safeAsal = normalizeOptionalText(asalInstansi);
        String safeKeperluan = normalizeOptionalText(keperluan);

        return kunjunganDao.createVisit(currentUser.getIdUser(), currentUser.getNama(), safeJenis, safeAsal, safeKeperluan);
    }

    public Kunjungan addGuestVisit(String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        String safeNama = requireText(namaPengunjung, "Nama pengunjung wajib diisi.");
        String safeJenis = normalizeJenisPengunjung(jenisPengunjung);
        String safeAsal = normalizeOptionalText(asalInstansi);
        String safeKeperluan = normalizeOptionalText(keperluan);

        return kunjunganDao.createVisit(null, safeNama, safeJenis, safeAsal, safeKeperluan);
    }

    public Kunjungan addManualVisit(String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        requireStaffOrAdmin();
        String safeNama = requireText(namaPengunjung, "Nama pengunjung wajib diisi.");
        String safeJenis = normalizeJenisPengunjung(jenisPengunjung);
        String safeAsal = normalizeOptionalText(asalInstansi);
        String safeKeperluan = normalizeOptionalText(keperluan);

        return kunjunganDao.createManualVisit(safeNama, safeJenis, safeAsal, safeKeperluan);
    }

    public List<Kunjungan> getRecentVisits(int limit) throws SQLException {
        requireStaffOrAdmin();
        int safeLimit = limit < 1 ? 50 : Math.min(limit, 200);
        return kunjunganDao.findRecentVisits(safeLimit);
    }

    public List<Kunjungan> searchVisits(String keyword, String status, int page, int pageSize) throws SQLException {
        requireStaffOrAdmin();
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize < 1 ? 50 : Math.min(pageSize, 200);
        int offset = (safePage - 1) * safePageSize;
        return kunjunganDao.searchVisits(normalizeOptionalText(keyword), normalizeOptionalText(status), safePageSize, offset);
    }

    public int countVisits(String keyword, String status) throws SQLException {
        requireStaffOrAdmin();
        return kunjunganDao.countVisits(normalizeOptionalText(keyword), normalizeOptionalText(status));
    }

    public Kunjungan finishVisit(int idKunjungan) throws SQLException {
        return updateVisitStatus(idKunjungan, "selesai");
    }

    public Kunjungan cancelVisit(int idKunjungan) throws SQLException {
        return updateVisitStatus(idKunjungan, "batal");
    }

    public Kunjungan getVisitById(int idKunjungan) throws SQLException {
        if (idKunjungan < 1) {
            throw new IllegalArgumentException("ID kunjungan tidak valid.");
        }
        Kunjungan kunjungan = kunjunganDao.findById(idKunjungan);
        if (kunjungan == null) {
            throw new IllegalStateException("Data kunjungan tidak ditemukan.");
        }
        return kunjungan;
    }

    private Kunjungan updateVisitStatus(int idKunjungan, String statusKunjungan) throws SQLException {
        if (idKunjungan < 1) {
            throw new IllegalArgumentException("ID kunjungan tidak valid.");
        }

        Kunjungan kunjungan = kunjunganDao.updateStatus(idKunjungan, statusKunjungan);
        if (kunjungan == null) {
            throw new IllegalStateException("Data kunjungan tidak ditemukan.");
        }
        return kunjungan;
    }

    private User requireLoggedInUser() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User harus login sebelum menambah kunjungan terdaftar.");
        }
        return currentUser;
    }

    private User requireStaffOrAdmin() {
        User currentUser = requireLoggedInUser();
        if (!currentUser.isStaff() && !currentUser.isAdmin()) {
            throw new IllegalStateException("Hanya staff atau admin yang boleh mengelola kunjungan manual.");
        }
        return currentUser;
    }

    private String normalizeJenisPengunjung(String jenisPengunjung) {
        String normalizedJenis = requireText(jenisPengunjung, "Jenis pengunjung wajib diisi.").toLowerCase();
        if (!"mahasiswa".equals(normalizedJenis) && !"dosen".equals(normalizedJenis) && !"staff".equals(normalizedJenis) && !"umum".equals(normalizedJenis)) {
            throw new IllegalArgumentException("Jenis pengunjung tidak valid.");
        }
        return normalizedJenis;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
