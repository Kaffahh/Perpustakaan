package com.mycompany.perpustakaan.api;

import com.mycompany.perpustakaan.controller.AdminReportController;
import com.mycompany.perpustakaan.controller.AuthController;
import com.mycompany.perpustakaan.controller.BookshelfController;
import com.mycompany.perpustakaan.controller.DashboardController;
import com.mycompany.perpustakaan.controller.HistoryController;
import com.mycompany.perpustakaan.controller.LoanController;
import com.mycompany.perpustakaan.controller.StaffBookController;
import com.mycompany.perpustakaan.controller.StaffLoanReturnController;
import com.mycompany.perpustakaan.controller.VisitController;
import com.mycompany.perpustakaan.model.Buku;
import com.mycompany.perpustakaan.model.Kunjungan;
import com.mycompany.perpustakaan.model.Peminjaman;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.FineCalculator;
import java.io.IOException;
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
    private final VisitController visitController;
    private final StaffBookController staffBookController;
    private final StaffLoanReturnController staffLoanReturnController;
    private final AdminReportController adminReportController;

    public LibraryApi() {
        this.authController = new AuthController();
        this.dashboardController = new DashboardController();
        this.bookshelfController = new BookshelfController();
        this.loanController = new LoanController();
        this.historyController = new HistoryController();
        this.visitController = new VisitController();
        this.staffBookController = new StaffBookController();
        this.staffLoanReturnController = new StaffLoanReturnController();
        this.adminReportController = new AdminReportController();
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

    public VisitResponse addRegisteredUserVisit(String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.addRegisteredUserVisit(jenisPengunjung, asalInstansi, keperluan);
            return VisitResponse.success("Kunjungan user terdaftar berhasil ditambahkan.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(exception.getMessage());
        }
    }

    public VisitResponse addGuestVisit(String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.addGuestVisit(namaPengunjung, jenisPengunjung, asalInstansi, keperluan);
            return VisitResponse.success("Kunjungan tamu berhasil ditambahkan.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(exception.getMessage());
        }
    }

    public VisitResponse finishVisit(int idKunjungan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.finishVisit(idKunjungan);
            return VisitResponse.success("Status kunjungan berhasil diubah menjadi selesai.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(exception.getMessage());
        }
    }

    public VisitResponse cancelVisit(int idKunjungan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.cancelVisit(idKunjungan);
            return VisitResponse.success("Status kunjungan berhasil diubah menjadi batal.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(exception.getMessage());
        }
    }

    public VisitSummary getVisitById(int idKunjungan) throws SQLException {
        return VisitSummary.from(visitController.getVisitById(idKunjungan));
    }

    public BookResponse addBook(BookRequest request) throws SQLException {
        try {
            Buku book = staffBookController.addBook(request);
            return BookResponse.success("Buku berhasil ditambahkan.", BookSummary.from(book));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return BookResponse.failure(exception.getMessage());
        }
    }

    public BookResponse updateBook(int idBuku, BookRequest request) throws SQLException {
        try {
            Buku book = staffBookController.updateBook(idBuku, request);
            return BookResponse.success("Buku berhasil diperbarui.", BookSummary.from(book));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return BookResponse.failure(exception.getMessage());
        }
    }

    public BookResponse updateBookStock(int idBuku, int stokTersedia, int stokTotal) throws SQLException {
        try {
            Buku book = staffBookController.updateStock(idBuku, stokTersedia, stokTotal);
            return BookResponse.success("Stok buku berhasil diperbarui.", BookSummary.from(book));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return BookResponse.failure(exception.getMessage());
        }
    }

    public BookResponse deleteBook(int idBuku) throws SQLException {
        try {
            staffBookController.deleteBook(idBuku);
            return BookResponse.success("Buku berhasil dihapus.", null);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return BookResponse.failure(exception.getMessage());
        }
    }

    public BookSummary getBookByIdForManagement(int idBuku) throws SQLException {
        return BookSummary.from(staffBookController.getBookById(idBuku));
    }

    public LoanResponse createLoanForUser(int idUser, int idBuku, int loanDays) throws SQLException {
        try {
            Peminjaman peminjaman = staffLoanReturnController.createLoanForUser(idUser, idBuku, loanDays);
            return LoanResponse.success("Peminjaman berhasil diproses.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return LoanResponse.failure(exception.getMessage());
        }
    }

    public LoanResponse processReturn(int idPeminjaman) throws SQLException {
        try {
            Peminjaman peminjaman = staffLoanReturnController.processReturn(idPeminjaman);
            return LoanResponse.success("Pengembalian berhasil diproses.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return LoanResponse.failure(exception.getMessage());
        }
    }

    public LoanManagementPage getLoansForManagement(String status, int page, int pageSize) throws SQLException {
        StaffLoanReturnController.LoanManagementResult result = staffLoanReturnController.getLoans(status, page, pageSize);
        return new LoanManagementPage(
                toLoanSummaries(result.getLoans()),
                result.getTotalItems(),
                result.getPage(),
                result.getPageSize(),
                result.getStatus());
    }

    public AdminDashboardSummary getAdminDashboardSummary() throws SQLException {
        return adminReportController.getAdminDashboardSummary();
    }

    public List<InventoryReportRow> getInventoryReport() throws SQLException {
        return adminReportController.getInventoryReport();
    }

    public List<LoanReportRow> getLoanReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        return adminReportController.getLoanReport(startDate, endDate);
    }

    public ReportExportResponse exportInventoryReport(String format, String outputDirectory) throws SQLException {
        try {
            String filePath = adminReportController.exportInventoryReport(format, outputDirectory).toAbsolutePath().toString();
            return ReportExportResponse.success("Laporan inventory berhasil diexport.", filePath, adminReportController.normalizeFormat(format));
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ReportExportResponse.failure(exception.getMessage());
        }
    }

    public ReportExportResponse exportLoanReport(String format, String outputDirectory, LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            String filePath = adminReportController.exportLoanReport(format, outputDirectory, startDate, endDate).toAbsolutePath().toString();
            return ReportExportResponse.success("Laporan peminjaman berhasil diexport.", filePath, adminReportController.normalizeFormat(format));
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ReportExportResponse.failure(exception.getMessage());
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
        BigDecimal runningFine = loan.getTanggalKembali() == null
                ? FineCalculator.calculateFine(loan.getTanggalJatuhTempo(), comparisonDate)
                : loan.getDenda();
        return LoanSummary.from(loan, lateDays, runningFine);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
