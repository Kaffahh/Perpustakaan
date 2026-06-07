package com.mycompany.perpustakaan.api;

import com.mycompany.perpustakaan.controller.AdminReportController;
import com.mycompany.perpustakaan.controller.AuthController;
import com.mycompany.perpustakaan.controller.BookshelfController;
import com.mycompany.perpustakaan.controller.DashboardController;
import com.mycompany.perpustakaan.controller.HistoryController;
import com.mycompany.perpustakaan.controller.LoanController;
import com.mycompany.perpustakaan.controller.MemberController;
import com.mycompany.perpustakaan.controller.StaffBookController;
import com.mycompany.perpustakaan.controller.StaffLoanReturnController;
import com.mycompany.perpustakaan.controller.VisitController;
import com.mycompany.perpustakaan.dao.CategoryDao;
import com.mycompany.perpustakaan.dao.FineDao;
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
    private final MemberController memberController;
    private final CategoryDao categoryDao;
    private final FineDao fineDao;

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
        this.memberController = new MemberController();
        this.categoryDao = new CategoryDao();
        this.fineDao = new FineDao();
    }

    public AuthResponse login(String username, String password) throws SQLException {
        User user = authController.login(username, password);
        if (user == null) {
            return AuthResponse.failure("Username atau password salah.");
        }

        return AuthResponse.success("Login berhasil.", UserSummary.from(user));
    }

    public MemberResponse register(MemberRequest request) throws SQLException {
        try {
            User member = authController.register(request);
            return MemberResponse.success("Register berhasil. Silakan login.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(exception.getMessage());
        }
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

    public List<BookSummary> getPopularBooks(int limit) throws SQLException {
        List<Buku> books = dashboardController.getPopularBooks(limit);
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

    public VisitResponse addManualVisit(String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.addManualVisit(namaPengunjung, jenisPengunjung, asalInstansi, keperluan);
            return VisitResponse.success("Kunjungan manual berhasil ditambahkan.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(exception.getMessage());
        }
    }

    public List<VisitSummary> getRecentVisits(int limit) throws SQLException {
        List<Kunjungan> visits = visitController.getRecentVisits(limit);
        List<VisitSummary> summaries = new ArrayList<>();
        for (Kunjungan visit : visits) {
            summaries.add(VisitSummary.from(visit));
        }
        return summaries;
    }

    public List<VisitSummary> searchVisits(String keyword, String status, int page, int pageSize) throws SQLException {
        List<Kunjungan> visits = visitController.searchVisits(keyword, status, page, pageSize);
        List<VisitSummary> summaries = new ArrayList<>();
        for (Kunjungan visit : visits) {
            summaries.add(VisitSummary.from(visit));
        }
        return summaries;
    }

    public int countVisits(String keyword, String status) throws SQLException {
        return visitController.countVisits(keyword, status);
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

    public LoanManagementPage searchLoansForManagement(String status, String keyword, int page, int pageSize) throws SQLException {
        StaffLoanReturnController.LoanManagementResult result = staffLoanReturnController.getLoans(status, keyword, page, pageSize);
        return new LoanManagementPage(
                toLoanSummaries(result.getLoans()),
                result.getTotalItems(),
                result.getPage(),
                result.getPageSize(),
                result.getStatus());
    }

    public List<LoanSummary> getPendingLoanRequests() throws SQLException {
        List<Peminjaman> loans = staffLoanReturnController.getPendingLoans();
        return toLoanSummaries(loans);
    }

    public LoanResponse approveLoanRequest(int idPeminjaman) throws SQLException {
        try {
            Peminjaman peminjaman = staffLoanReturnController.approveLoan(idPeminjaman);
            return LoanResponse.success("Peminjaman berhasil disetujui.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            return LoanResponse.failure(exception.getMessage());
        }
    }

    public LoanResponse rejectLoanRequest(int idPeminjaman) throws SQLException {
        try {
            Peminjaman peminjaman = staffLoanReturnController.rejectLoan(idPeminjaman);
            return LoanResponse.success("Peminjaman berhasil ditolak.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            return LoanResponse.failure(exception.getMessage());
        }
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

    public List<PopularBookReportRow> getPopularBookReport(int limit) throws SQLException {
        return adminReportController.getPopularBookReport(limit);
    }

    public List<VisitReportRow> getVisitReport(String keyword, String status) throws SQLException {
        return adminReportController.getVisitReport(keyword, status);
    }

    public List<CategorySummary> getCategorySummaries() throws SQLException {
        requireStaffOrAdmin("mengakses kategori.");
        return categoryDao.findCategorySummaries();
    }

    public BookResponse renameCategory(String oldName, String newName) throws SQLException {
        requireAdmin("mengubah kategori.");
        try {
            int updated = categoryDao.renameCategory(oldName, newName);
            return BookResponse.success("Kategori berhasil diperbarui untuk " + updated + " buku.", null);
        } catch (IllegalArgumentException exception) {
            return BookResponse.failure(exception.getMessage());
        }
    }

    public BookResponse clearCategory(String categoryName) throws SQLException {
        requireAdmin("menghapus kategori.");
        try {
            int updated = categoryDao.clearCategory(categoryName);
            return BookResponse.success("Kategori berhasil dilepas dari " + updated + " buku.", null);
        } catch (IllegalArgumentException exception) {
            return BookResponse.failure(exception.getMessage());
        }
    }

    public List<FineSummary> getFines(String keyword, String status, int page, int pageSize) throws SQLException {
        requireStaffOrAdmin("mengakses denda.");
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize < 1 ? 50 : Math.min(pageSize, 100);
        int offset = (safePage - 1) * safePageSize;
        return fineDao.findFines(normalizeText(keyword), normalizeText(status), safePageSize, offset);
    }

    public int countFines(String keyword, String status) throws SQLException {
        requireStaffOrAdmin("mengakses denda.");
        return fineDao.countFines(normalizeText(keyword), normalizeText(status));
    }

    public LoanResponse markFinePaid(int idPeminjaman) throws SQLException {
        requireStaffOrAdmin("memproses pembayaran denda.");
        try {
            fineDao.markFineStatus(idPeminjaman, "paid");
            return LoanResponse.success("Denda berhasil ditandai lunas.", null);
        } catch (IllegalArgumentException exception) {
            return LoanResponse.failure(exception.getMessage());
        }
    }

    public LoanResponse waiveFine(int idPeminjaman) throws SQLException {
        requireAdmin("menghapus/waive denda.");
        try {
            fineDao.markFineStatus(idPeminjaman, "waived");
            return LoanResponse.success("Denda berhasil di-waive.", null);
        } catch (IllegalArgumentException exception) {
            return LoanResponse.failure(exception.getMessage());
        }
    }

    public List<NotificationSummary> getNotifications(int limit) throws SQLException {
        User current = authController.getCurrentUser();
        if (current == null) {
            throw new IllegalStateException("User harus login sebelum melihat notifikasi.");
        }

        int safeLimit = limit < 1 ? 5 : Math.min(limit, 20);
        List<NotificationSummary> notifications = new ArrayList<>();
        String today = LocalDate.now().toString();

        if (current.isStaff() || current.isAdmin()) {
            int pendingLoans = staffLoanReturnController.getPendingLoans().size();
            if (pendingLoans > 0) {
                notifications.add(new NotificationSummary("loan_request", "Pending loan request",
                        pendingLoans + " request peminjaman menunggu persetujuan.", today));
            }

            int unpaidFines = fineDao.countFines(null, "unpaid");
            if (unpaidFines > 0) {
                notifications.add(new NotificationSummary("fine", "Denda belum lunas",
                        unpaidFines + " data denda masih berstatus unpaid.", today));
            }
        } else {
            for (LoanSummary loan : getCurrentLoans()) {
                if (loan.getHariTerlambat() > 0) {
                    notifications.add(new NotificationSummary("overdue", "Pinjaman terlambat",
                            loan.getJudulBuku() + " terlambat " + loan.getHariTerlambat() + " hari.", today));
                }
            }
        }

        if (notifications.isEmpty()) {
            notifications.add(new NotificationSummary("info", "Tidak ada notifikasi baru",
                    "Semua aktivitas terbaru sudah aman.", today));
        }
        return notifications.size() > safeLimit ? notifications.subList(0, safeLimit) : notifications;
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

    public MemberResponse addMember(MemberRequest request) throws SQLException {
        try {
            User member = memberController.addMember(request);
            return MemberResponse.success("Anggota berhasil ditambahkan.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(exception.getMessage());
        }
    }

    public MemberResponse updateMember(int idUser, MemberRequest request) throws SQLException {
        try {
            User member = memberController.updateMember(idUser, request);
            return MemberResponse.success("Anggota berhasil diperbarui.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(exception.getMessage());
        }
    }

    public MemberResponse suspendMember(int idUser) throws SQLException {
        try {
            User member = memberController.suspendMember(idUser);
            return MemberResponse.success("Anggota berhasil disuspend.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(exception.getMessage());
        }
    }

    public MemberResponse activateMember(int idUser) throws SQLException {
        try {
            User member = memberController.activateMember(idUser);
            return MemberResponse.success("Anggota berhasil diaktifkan.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(exception.getMessage());
        }
    }

    public MemberResponse deleteMember(int idUser) throws SQLException {
        try {
            memberController.deleteMember(idUser);
            return MemberResponse.success("Anggota berhasil dihapus.", null);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(exception.getMessage());
        }
    }

    public MemberPage searchMembers(String keyword, String statusAkun, int page, int pageSize) throws SQLException {
        int safePage = memberController.normalizePage(page);
        int safePageSize = memberController.normalizePageSize(pageSize);
        String safeStatus = memberController.normalizeStatus(statusAkun);
        List<User> members = memberController.searchMembers(keyword, safeStatus, safePage, safePageSize);
        int totalItems = memberController.countMembers(keyword, safeStatus);

        return new MemberPage(toMemberSummaries(members), totalItems, safePage, safePageSize, normalizeText(keyword), safeStatus);
    }

    public DashboardSummary getDashboardSummary(int latestLimit) throws SQLException {
        return new DashboardSummary(getCurrentUser(), getTotalBooks(), getPopularBooks(latestLimit), getLatestBooks(latestLimit));
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

    private List<MemberSummary> toMemberSummaries(List<User> members) {
        List<MemberSummary> summaries = new ArrayList<>();
        for (User member : members) {
            summaries.add(MemberSummary.from(member));
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

    private User requireStaffOrAdmin(String action) {
        User current = authController.getCurrentUser();
        if (current == null) {
            throw new IllegalStateException("User harus login sebelum " + action);
        }
        if (!current.isStaff() && !current.isAdmin()) {
            throw new IllegalStateException("Hanya staff atau admin yang boleh " + action);
        }
        return current;
    }

    private User requireAdmin(String action) {
        User current = authController.getCurrentUser();
        if (current == null) {
            throw new IllegalStateException("User harus login sebelum " + action);
        }
        if (!current.isAdmin()) {
            throw new IllegalStateException("Hanya admin yang boleh " + action);
        }
        return current;
    }
}
