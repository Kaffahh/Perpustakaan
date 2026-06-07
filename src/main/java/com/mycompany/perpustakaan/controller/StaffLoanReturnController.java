package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.dao.BukuDao;
import com.mycompany.perpustakaan.dao.PeminjamanDao;
import com.mycompany.perpustakaan.dao.UserDao;
import com.mycompany.perpustakaan.model.Buku;
import com.mycompany.perpustakaan.model.Peminjaman;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.FineCalculator;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StaffLoanReturnController {

    private static final int DEFAULT_LOAN_DAYS = 7;
    private static final int MAX_LOAN_DAYS = 30;
    private static final int MAX_ACTIVE_LOANS = 3;
    private static final int MAX_PAGE_SIZE = 100;

    private final PeminjamanDao peminjamanDao;
    private final BukuDao bukuDao;
    private final UserDao userDao;

    public StaffLoanReturnController() {
        this.peminjamanDao = new PeminjamanDao();
        this.bukuDao = new BukuDao();
        this.userDao = new UserDao();
    }

    public Peminjaman createLoanForUser(int idUser, int idBuku, int loanDays) throws SQLException {
        User staff = requireStaffOrAdmin();
        validateId(idUser, "ID user tidak valid.");
        validateId(idBuku, "ID buku tidak valid.");

        User borrower = userDao.findById(idUser);
        if (borrower == null) {
            throw new IllegalStateException("User peminjam tidak ditemukan.");
        }

        Buku book = bukuDao.findById(idBuku);
        if (book == null) {
            throw new IllegalStateException("Buku tidak ditemukan.");
        }

        if (peminjamanDao.countActiveLoansByUser(idUser) >= MAX_ACTIVE_LOANS) {
            throw new IllegalStateException("User sudah mencapai maksimal " + MAX_ACTIVE_LOANS + " peminjaman aktif.");
        }

        if (peminjamanDao.hasActiveLoanForBook(idUser, idBuku)) {
            throw new IllegalStateException("User masih meminjam buku yang sama.");
        }

        LocalDate tanggalPinjam = LocalDate.now();
        LocalDate tanggalJatuhTempo = tanggalPinjam.plusDays(normalizeLoanDays(loanDays));
        Peminjaman peminjaman = peminjamanDao.createLoan(idUser, idBuku, tanggalPinjam, tanggalJatuhTempo, staff.getIdUser());
        if (peminjaman == null) {
            throw new IllegalStateException("Stok buku tidak tersedia.");
        }
        return peminjaman;
    }

    public Peminjaman processReturn(int idPeminjaman) throws SQLException {
        requireStaffOrAdmin();
        validateId(idPeminjaman, "ID peminjaman tidak valid.");

        Peminjaman peminjaman = peminjamanDao.findById(idPeminjaman);
        if (peminjaman == null) {
            throw new IllegalStateException("Data peminjaman tidak ditemukan.");
        }
        if (peminjaman.getTanggalKembali() != null || "dikembalikan".equalsIgnoreCase(peminjaman.getStatus())) {
            throw new IllegalStateException("Peminjaman sudah dikembalikan.");
        }

        LocalDate tanggalKembali = LocalDate.now();
        BigDecimal denda = FineCalculator.calculateFine(peminjaman.getTanggalJatuhTempo(), tanggalKembali);
        String status = denda.signum() > 0 ? "terlambat" : "dikembalikan";

        return peminjamanDao.processReturn(idPeminjaman, tanggalKembali, status, denda);
    }

    public Peminjaman approveLoan(int idPeminjaman) throws SQLException {
        requireStaffOrAdmin();
        validateId(idPeminjaman, "ID peminjaman tidak valid.");
        return peminjamanDao.approveLoan(idPeminjaman);
    }

    public Peminjaman rejectLoan(int idPeminjaman) throws SQLException {
        requireStaffOrAdmin();
        validateId(idPeminjaman, "ID peminjaman tidak valid.");
        return peminjamanDao.rejectLoan(idPeminjaman);
    }

    public List<Peminjaman> getPendingLoans() throws SQLException {
        requireStaffOrAdmin();
        return peminjamanDao.findPendingLoans();
    }

    public LoanManagementResult getPendingLoans(String keyword, int page, int pageSize) throws SQLException {
        requireStaffOrAdmin();
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        String safeKeyword = normalizeKeyword(keyword);
        int offset = (safePage - 1) * safePageSize;

        List<Peminjaman> loans = peminjamanDao.findPendingLoans(safeKeyword, safePageSize, offset);
        int totalItems = peminjamanDao.countPendingLoans(safeKeyword);
        return new LoanManagementResult(loans, totalItems, safePage, safePageSize, "menunggu");
    }

    public LoanManagementResult getLoans(String status, int page, int pageSize) throws SQLException {
        return getLoans(status, null, page, pageSize);
    }

    public LoanManagementResult getLoans(String status, String keyword, int page, int pageSize) throws SQLException {
        return getLoans(status, keyword, null, null, page, pageSize);
    }

    public LoanManagementResult getLoans(String status, String keyword, LocalDate startDate, LocalDate endDate, int page, int pageSize) throws SQLException {
        requireStaffOrAdmin();
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        String safeStatus = normalizeStatus(status);
        String safeKeyword = normalizeKeyword(keyword);
        int offset = (safePage - 1) * safePageSize;

        List<Peminjaman> loans = peminjamanDao.findLoansForManagement(safeStatus, safeKeyword, startDate, endDate, safePageSize, offset);
        int totalItems = peminjamanDao.countLoansForManagement(safeStatus, safeKeyword, startDate, endDate);
        return new LoanManagementResult(loans, totalItems, safePage, safePageSize, safeStatus);
    }

    public int normalizeLoanDays(int loanDays) {
        if (loanDays < 1) {
            return DEFAULT_LOAN_DAYS;
        }
        return Math.min(loanDays, MAX_LOAN_DAYS);
    }

    public int normalizePage(int page) {
        if (page < 1) {
            return 1;
        }
        return page;
    }

    public int normalizePageSize(int pageSize) {
        if (pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    public String normalizeStatus(String status) {
        if (status == null || status.isBlank() || "semua".equalsIgnoreCase(status) || "all".equalsIgnoreCase(status)) {
            return null;
        }

        String normalizedStatus = status.trim().toLowerCase();
        if (!"aktif".equals(normalizedStatus) && !"dipinjam".equals(normalizedStatus) && !"dikembalikan".equals(normalizedStatus) && !"terlambat".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Status peminjaman tidak valid.");
        }
        return normalizedStatus;
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private User requireStaffOrAdmin() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User harus login sebelum mengakses loans & returns.");
        }
        if (!currentUser.isStaff() && !currentUser.isAdmin()) {
            throw new IllegalStateException("Hanya staff atau admin yang boleh mengakses loans & returns.");
        }
        return currentUser;
    }

    private void validateId(int id, String message) {
        if (id < 1) {
            throw new IllegalArgumentException(message);
        }
    }

    public static class LoanManagementResult {

        private final List<Peminjaman> loans;
        private final int totalItems;
        private final int page;
        private final int pageSize;
        private final String status;

        public LoanManagementResult(List<Peminjaman> loans, int totalItems, int page, int pageSize, String status) {
            this.loans = loans;
            this.totalItems = totalItems;
            this.page = page;
            this.pageSize = pageSize;
            this.status = status;
        }

        public List<Peminjaman> getLoans() {
            return loans;
        }

        public int getTotalItems() {
            return totalItems;
        }

        public int getPage() {
            return page;
        }

        public int getPageSize() {
            return pageSize;
        }

        public String getStatus() {
            return status;
        }
    }
}
