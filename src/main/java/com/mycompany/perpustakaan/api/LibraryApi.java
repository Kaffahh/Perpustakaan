package com.mycompany.perpustakaan.api;

import com.mycompany.perpustakaan.controller.AuthController;
import com.mycompany.perpustakaan.controller.BookshelfController;
import com.mycompany.perpustakaan.controller.DashboardController;
import com.mycompany.perpustakaan.controller.HistoryController;
import com.mycompany.perpustakaan.controller.LoanController;
import com.mycompany.perpustakaan.model.Buku;
import com.mycompany.perpustakaan.model.Peminjaman;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.FineCalculator;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LibraryApi {

    private final AuthController authController;
    private final DashboardController dashboardController;
    private final BookshelfController bookshelfController;
    private final LoanController loanController;
    private final HistoryController historyController;

    public LibraryApi() {
        this.authController = new AuthController();
        this.dashboardController = new DashboardController();
        this.bookshelfController = new BookshelfController();
        this.loanController = new LoanController();
        this.historyController = new HistoryController();
    }

    public AuthResponse login(String username, String password) throws SQLException {
        User user = authController.login(username, password);
        if (user == null) {
            return AuthResponse.failure("Username atau password salah.");
        }

        return AuthResponse.success("Login berhasil.", UserSummary.from(user));
    }

    public void logout() {
        authController.logout();
    }

    public boolean isLoggedIn() {
        return authController.isLoggedIn();
    }

    public UserSummary getCurrentUser() {
        return UserSummary.from(authController.getCurrentUser());
    }

    public int getTotalBooks() throws SQLException {
        return dashboardController.getTotalBooks();
    }

    public List<BookSummary> getLatestBooks(int limit) throws SQLException {
        List<Buku> books = dashboardController.getLatestBooks(limit);
        return toBookSummaries(books);
    }

    public List<BookSummary> searchBooks(String keyword, int limit, int offset) throws SQLException {
        List<Buku> books = dashboardController.searchBooks(keyword, limit, offset);
        return toBookSummaries(books);
    }

    public BookshelfPage getBookshelfPage(String keyword, String kategori, int page, int pageSize) throws SQLException {
        int safePage = bookshelfController.normalizePage(page);
        int safePageSize = bookshelfController.normalizePageSize(pageSize);
        List<Buku> books = bookshelfController.getBooks(keyword, kategori, safePage, safePageSize);
        int totalItems = bookshelfController.countBooks(keyword, kategori);

        return new BookshelfPage(toBookSummaries(books), totalItems, safePage, safePageSize, normalizeText(keyword), normalizeText(kategori));
    }

    public List<String> getBookCategories() throws SQLException {
        return bookshelfController.getCategories();
    }

    public LoanResponse requestLoan(int idBuku, int loanDays) throws SQLException {
        try {
            Peminjaman peminjaman = loanController.requestLoan(idBuku, loanDays);
            return LoanResponse.success("Peminjaman berhasil diajukan.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return LoanResponse.failure(exception.getMessage());
        }
    }

    public List<LoanSummary> getCurrentLoans() throws SQLException {
        List<Peminjaman> loans = loanController.getCurrentLoans();
        return toLoanSummaries(loans);
    }

    public HistoryPage getLoanHistory(String status, int page, int pageSize) throws SQLException {
        try {
            int safePage = historyController.normalizePage(page);
            int safePageSize = historyController.normalizePageSize(pageSize);
            String safeStatus = historyController.normalizeStatus(status);
            List<Peminjaman> loans = historyController.getUserLoanHistory(safeStatus, safePage, safePageSize);
            int totalItems = historyController.countUserLoanHistory(safeStatus);

            return new HistoryPage(toLoanSummaries(loans), totalItems, safePage, safePageSize, safeStatus);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            throw exception;
        }
    }

    public DashboardSummary getDashboardSummary(int latestLimit) throws SQLException {
        return new DashboardSummary(getCurrentUser(), getTotalBooks(), getLatestBooks(latestLimit));
    }

    private List<BookSummary> toBookSummaries(List<Buku> books) {
        List<BookSummary> summaries = new ArrayList<>();
        for (Buku book : books) {
            summaries.add(BookSummary.from(book));
        }
        return summaries;
    }

    private List<LoanSummary> toLoanSummaries(List<Peminjaman> loans) {
        List<LoanSummary> summaries = new ArrayList<>();
        for (Peminjaman loan : loans) {
            summaries.add(toLoanSummary(loan));
        }
        return summaries;
    }

    private LoanSummary toLoanSummary(Peminjaman loan) {
        LocalDate comparisonDate = loan.getTanggalKembali() == null ? LocalDate.now() : loan.getTanggalKembali();
        int lateDays = FineCalculator.countLateDays(loan.getTanggalJatuhTempo(), comparisonDate);
        BigDecimal runningFine = FineCalculator.calculateFine(loan.getTanggalJatuhTempo(), comparisonDate);
        return LoanSummary.from(loan, lateDays, runningFine);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
