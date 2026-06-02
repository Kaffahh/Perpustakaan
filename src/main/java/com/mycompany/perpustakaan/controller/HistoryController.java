package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.dao.PeminjamanDao;
import com.mycompany.perpustakaan.model.Peminjaman;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;
import java.util.List;

public class HistoryController {

    private static final int MAX_PAGE_SIZE = 100;

    private final PeminjamanDao peminjamanDao;

    public HistoryController() {
        this.peminjamanDao = new PeminjamanDao();
    }

    public List<Peminjaman> getUserLoanHistory(String status, int page, int pageSize) throws SQLException {
        User currentUser = requireLoggedInUser();
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;

        return peminjamanDao.findLoanHistoryByUser(currentUser.getIdUser(), normalizeStatus(status), safePageSize, offset);
    }

    public int countUserLoanHistory(String status) throws SQLException {
        User currentUser = requireLoggedInUser();
        return peminjamanDao.countLoanHistoryByUser(currentUser.getIdUser(), normalizeStatus(status));
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
        if (!"dipinjam".equals(normalizedStatus) && !"dikembalikan".equals(normalizedStatus) && !"terlambat".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Status history tidak valid.");
        }

        return normalizedStatus;
    }

    private User requireLoggedInUser() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User harus login sebelum mengakses history.");
        }
        return currentUser;
    }
}
