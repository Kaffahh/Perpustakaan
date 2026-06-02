package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.dao.PeminjamanDao;
import com.mycompany.perpustakaan.model.Peminjaman;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class LoanController {

    private static final int DEFAULT_LOAN_DAYS = 7;
    private static final int MAX_LOAN_DAYS = 30;
    private static final int MAX_ACTIVE_LOANS = 3;

    private final PeminjamanDao peminjamanDao;

    public LoanController() {
        this.peminjamanDao = new PeminjamanDao();
    }

    public Peminjaman requestLoan(int idBuku, int loanDays) throws SQLException {
        User currentUser = requireLoggedInUser();
        int safeLoanDays = normalizeLoanDays(loanDays);

        if (idBuku < 1) {
            throw new IllegalArgumentException("ID buku tidak valid.");
        }

        int activeLoanCount = peminjamanDao.countActiveLoansByUser(currentUser.getIdUser());
        if (activeLoanCount >= MAX_ACTIVE_LOANS) {
            throw new IllegalStateException("Maksimal peminjaman aktif adalah " + MAX_ACTIVE_LOANS + " buku.");
        }

        if (peminjamanDao.hasActiveLoanForBook(currentUser.getIdUser(), idBuku)) {
            throw new IllegalStateException("Buku ini masih sedang dipinjam oleh user yang sama.");
        }

        LocalDate tanggalPinjam = LocalDate.now();
        LocalDate tanggalJatuhTempo = tanggalPinjam.plusDays(safeLoanDays);
        Peminjaman peminjaman = peminjamanDao.createLoan(currentUser.getIdUser(), idBuku, tanggalPinjam, tanggalJatuhTempo, null);
        if (peminjaman == null) {
            throw new IllegalStateException("Stok buku tidak tersedia.");
        }

        return peminjaman;
    }

    public List<Peminjaman> getCurrentLoans() throws SQLException {
        User currentUser = requireLoggedInUser();
        return peminjamanDao.findActiveLoansByUser(currentUser.getIdUser());
    }

    public int normalizeLoanDays(int loanDays) {
        if (loanDays < 1) {
            return DEFAULT_LOAN_DAYS;
        }
        return Math.min(loanDays, MAX_LOAN_DAYS);
    }

    private User requireLoggedInUser() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User harus login sebelum mengakses loan page.");
        }
        return currentUser;
    }
}
