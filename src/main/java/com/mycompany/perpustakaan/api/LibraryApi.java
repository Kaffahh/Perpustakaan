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
import com.mycompany.perpustakaan.dao.NotificationDao;
import com.mycompany.perpustakaan.model.Buku;
import com.mycompany.perpustakaan.model.Kunjungan;
import com.mycompany.perpustakaan.model.Peminjaman;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.FineCalculator;
import com.mycompany.perpustakaan.utils.PasswordHasher;
import com.mycompany.perpustakaan.utils.ReportExporter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
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
    private final NotificationDao notificationDao;
    private final ReportExporter reportExporter;

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
        this.notificationDao = new NotificationDao();
        this.reportExporter = new ReportExporter();
    }

    public AuthResponse login(String username, String password) throws SQLException {
        User user = authController.login(username, password);
        if (user == null) {
            return AuthResponse.failure("Username atau password salah.");
        }

        return AuthResponse.success("Login berhasil.", UserSummary.from(user));
    }

    public SessionLoginResponse loginPersistent(String username, String password) throws SQLException {
        PersistentSession session = authController.loginPersistent(username, password);
        if (session == null) {
            return SessionLoginResponse.failure("Username atau password salah.");
        }
        return SessionLoginResponse.success(
                "Login berhasil.",
                UserSummary.from(session.getUser()),
                session.getToken(),
                session.getExpiresAt());
    }

    public AuthResponse restoreSession(String sessionToken) throws SQLException {
        User user = authController.restoreSession(sessionToken);
        if (user == null) {
            return AuthResponse.failure("Session tidak valid atau sudah kedaluwarsa.");
        }
        return AuthResponse.success("Session berhasil dipulihkan.", UserSummary.from(user));
    }

    public MemberResponse register(MemberRequest request) throws SQLException {
        try {
            User member = authController.register(request);
            return MemberResponse.success("Register berhasil. Silakan login.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public void logout() {
        authController.logout();
    }

    public void logout(String sessionToken) throws SQLException {
        authController.logout(sessionToken);
    }

    public void revokeAllCurrentUserSessions() throws SQLException {
        authController.revokeAllCurrentUserSessions();
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
            notificationDao.createNotification("loan_request", "Request peminjaman baru",
                    "Ada request peminjaman dari " + getCurrentUser().getNama() + ".", null, "staff");
            return LoanResponse.success("Peminjaman berhasil diajukan.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return LoanResponse.failure(messageOf(exception, "Operasi gagal."));
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
            return VisitResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public VisitResponse addGuestVisit(String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.addGuestVisit(namaPengunjung, jenisPengunjung, asalInstansi, keperluan);
            return VisitResponse.success("Kunjungan tamu berhasil ditambahkan.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public VisitResponse addManualVisit(String namaPengunjung, String jenisPengunjung, String asalInstansi, String keperluan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.addManualVisit(namaPengunjung, jenisPengunjung, asalInstansi, keperluan);
            return VisitResponse.success("Kunjungan manual berhasil ditambahkan.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(messageOf(exception, "Operasi gagal."));
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

    public VisitPage getVisitPage(String keyword, String status, int page, int pageSize) throws SQLException {
        return getVisitPage(keyword, status, null, null, page, pageSize);
    }

    public VisitPage getVisitPage(String keyword, String status, LocalDate startDate, LocalDate endDate, int page, int pageSize) throws SQLException {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize, 200);
        List<VisitSummary> visits = searchVisits(keyword, status, startDate, endDate, safePage, safePageSize);
        int totalItems = countVisits(keyword, status, startDate, endDate);
        return new VisitPage(visits, totalItems, safePage, safePageSize, normalizeText(keyword), normalizeText(status));
    }

    public List<VisitSummary> searchVisits(String keyword, String status, int page, int pageSize) throws SQLException {
        return searchVisits(keyword, status, null, null, page, pageSize);
    }

    public List<VisitSummary> searchVisits(String keyword, String status, LocalDate startDate, LocalDate endDate, int page, int pageSize) throws SQLException {
        List<Kunjungan> visits = visitController.searchVisits(
                normalizeText(keyword), normalizeText(status), startDate, endDate, page, pageSize);
        List<VisitSummary> summaries = new ArrayList<>();
        for (Kunjungan visit : visits) {
            summaries.add(VisitSummary.from(visit));
        }
        return summaries;
    }

    public int countVisits(String keyword, String status) throws SQLException {
        return countVisits(keyword, status, null, null);
    }

    public int countVisits(String keyword, String status, LocalDate startDate, LocalDate endDate) throws SQLException {
        return visitController.countVisits(normalizeText(keyword), normalizeText(status), startDate, endDate);
    }

    public VisitResponse finishVisit(int idKunjungan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.finishVisit(idKunjungan);
            return VisitResponse.success("Status kunjungan berhasil diubah menjadi selesai.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public VisitResponse cancelVisit(int idKunjungan) throws SQLException {
        try {
            Kunjungan kunjungan = visitController.cancelVisit(idKunjungan);
            return VisitResponse.success("Status kunjungan berhasil diubah menjadi batal.", VisitSummary.from(kunjungan));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return VisitResponse.failure(messageOf(exception, "Operasi gagal."));
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
            return BookResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public BookResponse updateBook(int idBuku, BookRequest request) throws SQLException {
        try {
            Buku book = staffBookController.updateBook(idBuku, request);
            return BookResponse.success("Buku berhasil diperbarui.", BookSummary.from(book));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return BookResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public BookResponse updateBookStock(int idBuku, int stokTersedia, int stokTotal) throws SQLException {
        try {
            Buku book = staffBookController.updateStock(idBuku, stokTersedia, stokTotal);
            return BookResponse.success("Stok buku berhasil diperbarui.", BookSummary.from(book));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return BookResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public BookResponse deleteBook(int idBuku) throws SQLException {
        try {
            staffBookController.deleteBook(idBuku);
            return BookResponse.success("Buku berhasil dihapus.", null);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return BookResponse.failure(messageOf(exception, "Operasi gagal."));
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
            return LoanResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public LoanResponse processReturn(int idPeminjaman) throws SQLException {
        try {
            Peminjaman peminjaman = staffLoanReturnController.processReturn(idPeminjaman);
            return LoanResponse.success("Pengembalian berhasil diproses.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return LoanResponse.failure(messageOf(exception, "Operasi gagal."));
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
        return searchLoansForManagement(status, keyword, null, null, page, pageSize);
    }

    public LoanManagementPage searchLoansForManagement(String status, String keyword, LocalDate startDate, LocalDate endDate, int page, int pageSize) throws SQLException {
        StaffLoanReturnController.LoanManagementResult result = staffLoanReturnController.getLoans(status, keyword, startDate, endDate, page, pageSize);
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

    public LoanManagementPage getPendingLoanRequests(int page, int pageSize) throws SQLException {
        return getPendingLoanRequests(null, page, pageSize);
    }

    public LoanManagementPage getPendingLoanRequests(String keyword, int page, int pageSize) throws SQLException {
        StaffLoanReturnController.LoanManagementResult result = staffLoanReturnController.getPendingLoans(keyword, page, pageSize);
        return new LoanManagementPage(
                toLoanSummaries(result.getLoans()),
                result.getTotalItems(),
                result.getPage(),
                result.getPageSize(),
                result.getStatus());
    }

    public LoanResponse approveLoanRequest(int idPeminjaman) throws SQLException {
        try {
            Peminjaman peminjaman = staffLoanReturnController.approveLoan(idPeminjaman);
            notificationDao.createNotification("loan_approved", "Peminjaman disetujui",
                    "Request peminjaman kamu disetujui.", peminjaman.getIdUser(), null);
            return LoanResponse.success("Peminjaman berhasil disetujui.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            return LoanResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public LoanResponse rejectLoanRequest(int idPeminjaman) throws SQLException {
        try {
            Peminjaman peminjaman = staffLoanReturnController.rejectLoan(idPeminjaman);
            notificationDao.createNotification("loan_rejected", "Peminjaman ditolak",
                    "Request peminjaman kamu ditolak.", peminjaman.getIdUser(), null);
            return LoanResponse.success("Peminjaman berhasil ditolak.", toLoanSummary(peminjaman));
        } catch (IllegalArgumentException | IllegalStateException | SQLException exception) {
            return LoanResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public AdminDashboardSummary getAdminDashboardSummary() throws SQLException {
        return adminReportController.getAdminDashboardSummary();
    }

    public List<InventoryReportRow> getInventoryReport() throws SQLException {
        return adminReportController.getInventoryReport();
    }

    public List<InventoryReportRow> getInventoryReport(String keyword, String kategori, int page, int pageSize) throws SQLException {
        return adminReportController.getInventoryReport(keyword, kategori, page, pageSize);
    }

    public int countInventoryReport(String keyword, String kategori) throws SQLException {
        return adminReportController.countInventoryReport(keyword, kategori);
    }

    public InventoryReportPage getInventoryReportPage(String keyword, String kategori, int page, int pageSize) throws SQLException {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize, 200);
        return new InventoryReportPage(
                getInventoryReport(keyword, kategori, safePage, safePageSize),
                countInventoryReport(keyword, kategori),
                safePage,
                safePageSize,
                normalizeText(keyword),
                normalizeText(kategori));
    }

    public List<LoanReportRow> getLoanReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        return adminReportController.getLoanReport(startDate, endDate);
    }

    public List<PopularBookReportRow> getPopularBookReport(int limit) throws SQLException {
        return adminReportController.getPopularBookReport(limit);
    }

    public List<PopularBookReportRow> getPopularBookReport(int page, int pageSize) throws SQLException {
        return adminReportController.getPopularBookReport(page, pageSize);
    }

    public int countPopularBookReport() throws SQLException {
        return adminReportController.countPopularBookReport();
    }

    public PopularBookReportPage getPopularBookReportPage(int page, int pageSize) throws SQLException {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize, 100);
        return new PopularBookReportPage(
                getPopularBookReport(safePage, safePageSize),
                countPopularBookReport(),
                safePage,
                safePageSize);
    }

    public List<VisitReportRow> getVisitReport(String keyword, String status) throws SQLException {
        return adminReportController.getVisitReport(keyword, status);
    }

    public VisitReportPage getVisitReportPage(String keyword, String status, int page, int pageSize) throws SQLException {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize, 200);
        return new VisitReportPage(
                adminReportController.getVisitReport(keyword, status, safePage, safePageSize),
                adminReportController.countVisitReport(keyword, status),
                safePage,
                safePageSize,
                normalizeText(keyword),
                normalizeText(status));
    }

    public List<CategorySummary> getCategorySummaries() throws SQLException {
        requireStaffOrAdmin("mengakses kategori.");
        return categoryDao.findCategorySummaries();
    }

    public BookResponse createCategory(String categoryName) throws SQLException {
        requireAdmin("menambah kategori.");
        try {
            CategorySummary category = categoryDao.createCategory(categoryName);
            return BookResponse.success("Kategori " + category.getNamaKategori() + " berhasil ditambahkan.", null);
        } catch (IllegalArgumentException | SQLException exception) {
            return BookResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public BookResponse renameCategory(String oldName, String newName) throws SQLException {
        requireAdmin("mengubah kategori.");
        try {
            int updated = categoryDao.renameCategory(oldName, newName);
            return BookResponse.success("Kategori berhasil diperbarui untuk " + updated + " buku.", null);
        } catch (IllegalArgumentException exception) {
            return BookResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public BookResponse clearCategory(String categoryName) throws SQLException {
        requireAdmin("menghapus kategori.");
        try {
            int updated = categoryDao.clearCategory(categoryName);
            return BookResponse.success("Kategori berhasil dilepas dari " + updated + " buku.", null);
        } catch (IllegalArgumentException exception) {
            return BookResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public List<FineSummary> getFines(String keyword, String status, int page, int pageSize) throws SQLException {
        requireStaffOrAdmin("mengakses denda.");
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize, 100);
        int offset = (safePage - 1) * safePageSize;
        return fineDao.findFines(normalizeText(keyword), normalizeText(status), safePageSize, offset);
    }

    public int countFines(String keyword, String status) throws SQLException {
        requireStaffOrAdmin("mengakses denda.");
        return fineDao.countFines(normalizeText(keyword), normalizeText(status));
    }

    public FinePage getFinePage(String keyword, String status, int page, int pageSize) throws SQLException {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize, 100);
        return new FinePage(
                getFines(keyword, status, safePage, safePageSize),
                countFines(keyword, status),
                safePage,
                safePageSize,
                normalizeText(keyword),
                normalizeText(status));
    }

    public List<FinePaymentLogSummary> getFinePaymentLogs(int idPeminjaman) throws SQLException {
        requireStaffOrAdmin("melihat riwayat pembayaran denda.");
        return fineDao.findFinePaymentLogs(idPeminjaman);
    }

    public LoanResponse markFinePaid(int idPeminjaman) throws SQLException {
        requireStaffOrAdmin("memproses pembayaran denda.");
        try {
            User current = authController.getCurrentUser();
            fineDao.markFineStatus(idPeminjaman, "paid", current == null ? null : current.getIdUser(), "Ditandai lunas dari aplikasi.");
            notificationDao.createNotification("fine_paid", "Denda lunas",
                    "Denda peminjaman ID " + idPeminjaman + " sudah ditandai lunas.", null, "staff");
            return LoanResponse.success("Denda berhasil ditandai lunas.", null);
        } catch (IllegalArgumentException exception) {
            return LoanResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public LoanResponse waiveFine(int idPeminjaman) throws SQLException {
        requireAdmin("menghapus/waive denda.");
        try {
            User current = authController.getCurrentUser();
            fineDao.markFineStatus(idPeminjaman, "waived", current == null ? null : current.getIdUser(), "Denda di-waive admin.");
            notificationDao.createNotification("fine_waived", "Denda di-waive",
                    "Denda peminjaman ID " + idPeminjaman + " di-waive admin.", null, "staff");
            return LoanResponse.success("Denda berhasil di-waive.", null);
        } catch (IllegalArgumentException exception) {
            return LoanResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public List<NotificationSummary> getNotifications(int limit) throws SQLException {
        User current = authController.getCurrentUser();
        if (current == null) {
            throw new IllegalStateException("User harus login sebelum melihat notifikasi.");
        }

        int safeLimit = limit < 1 ? 5 : Math.min(limit, 20);
        List<NotificationSummary> storedNotifications = notificationDao.findNotifications(current.getIdUser(), current.getRole(), safeLimit);
        if (!storedNotifications.isEmpty()) {
            return storedNotifications;
        }
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

    public MemberResponse markAllNotificationsRead() throws SQLException {
        User current = authController.getCurrentUser();
        if (current == null) {
            return MemberResponse.failure("User harus login sebelum mengubah notifikasi.");
        }
        notificationDao.markAllRead(current.getIdUser(), current.getRole());
        return MemberResponse.success("Semua notifikasi sudah ditandai dibaca.", null);
    }

    public ReportExportResponse exportInventoryReport(String format, String outputDirectory) throws SQLException {
        try {
            String filePath = adminReportController.exportInventoryReport(format, outputDirectory).toAbsolutePath().toString();
            return ReportExportResponse.success("Laporan inventory berhasil diexport.", filePath, adminReportController.normalizeFormat(format));
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ReportExportResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public ReportExportResponse exportLoanReport(String format, String outputDirectory, LocalDate startDate, LocalDate endDate) throws SQLException {
        try {
            String filePath = adminReportController.exportLoanReport(format, outputDirectory, startDate, endDate).toAbsolutePath().toString();
            return ReportExportResponse.success("Laporan peminjaman berhasil diexport.", filePath, adminReportController.normalizeFormat(format));
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ReportExportResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public ReportExportResponse exportVisitReport(String format, String outputDirectory, String keyword, String status) throws SQLException {
        try {
            String filePath = adminReportController.exportVisitReport(format, outputDirectory, keyword, status).toAbsolutePath().toString();
            return ReportExportResponse.success("Laporan kunjungan berhasil diexport.", filePath, adminReportController.normalizeFormat(format));
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ReportExportResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public ReportExportResponse exportFineReport(String format, String outputDirectory, String keyword, String status) throws SQLException {
        requireStaffOrAdmin("export laporan denda.");
        try {
            String safeFormat = adminReportController.normalizeFormat(format);
            Path filePath = reportExporter.exportFines(
                    getFines(keyword, status, 1, 5000),
                    safeFormat,
                    normalizeOutputDirectory(outputDirectory));
            return ReportExportResponse.success("Laporan denda berhasil diexport.", filePath.toAbsolutePath().toString(), safeFormat);
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ReportExportResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public ReportExportResponse exportMemberReport(String format, String outputDirectory, String keyword, String statusAkun) throws SQLException {
        requireAdmin("export laporan member.");
        try {
            String safeFormat = adminReportController.normalizeFormat(format);
            Path filePath = reportExporter.exportMembers(
                    searchMembers(keyword, statusAkun, 1, 5000).getMembers(),
                    safeFormat,
                    normalizeOutputDirectory(outputDirectory));
            return ReportExportResponse.success("Laporan member berhasil diexport.", filePath.toAbsolutePath().toString(), safeFormat);
        } catch (IllegalArgumentException | IllegalStateException | IOException exception) {
            return ReportExportResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public MemberResponse addMember(MemberRequest request) throws SQLException {
        try {
            User member = memberController.addMember(request);
            return MemberResponse.success("Anggota berhasil ditambahkan.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public MemberResponse updateMember(int idUser, MemberRequest request) throws SQLException {
        try {
            User member = memberController.updateMember(idUser, request);
            return MemberResponse.success("Anggota berhasil diperbarui.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public MemberResponse suspendMember(int idUser) throws SQLException {
        try {
            User member = memberController.suspendMember(idUser);
            return MemberResponse.success("Anggota berhasil disuspend.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public MemberResponse activateMember(int idUser) throws SQLException {
        try {
            User member = memberController.activateMember(idUser);
            return MemberResponse.success("Anggota berhasil diaktifkan.", MemberSummary.from(member));
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(messageOf(exception, "Operasi gagal."));
        }
    }

    public MemberResponse deleteMember(int idUser) throws SQLException {
        try {
            memberController.deleteMember(idUser);
            return MemberResponse.success("Anggota berhasil dihapus.", null);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return MemberResponse.failure(messageOf(exception, "Operasi gagal."));
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

    public MemberResponse updateCurrentProfile(ProfileRequest request) throws SQLException {
        User current = authController.getCurrentUser();
        if (current == null) {
            return MemberResponse.failure("User harus login sebelum mengubah profil.");
        }
        if (request == null || request.getNama() == null || request.getNama().trim().isEmpty()) {
            return MemberResponse.failure("Nama wajib diisi.");
        }
        String email = request.getEmail() == null ? null : request.getEmail().trim();
        if (email != null && !email.isEmpty() && !email.contains("@")) {
            return MemberResponse.failure("Format email tidak valid.");
        }
        User updated = new com.mycompany.perpustakaan.dao.UserDao()
                .updateProfile(current.getIdUser(), request.getNama().trim(), email);
        return MemberResponse.success("Profil berhasil diperbarui.", MemberSummary.from(updated));
    }

    public MemberResponse changeCurrentPassword(String oldPassword, String newPassword) throws SQLException {
        User current = authController.getCurrentUser();
        if (current == null) {
            return MemberResponse.failure("User harus login sebelum mengganti password.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return MemberResponse.failure("Password baru minimal 6 karakter.");
        }
        if (!PasswordHasher.matches(oldPassword == null ? "" : oldPassword, current.getPassword())) {
            return MemberResponse.failure("Password lama salah.");
        }
        User updated = new com.mycompany.perpustakaan.dao.UserDao()
                .updatePassword(current.getIdUser(), PasswordHasher.hash(newPassword));
        return MemberResponse.success("Password berhasil diganti.", MemberSummary.from(updated));
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

    private String messageOf(Exception exception, String fallback) {
        if (exception == null || exception.getMessage() == null || exception.getMessage().isBlank()) {
            return fallback;
        }
        return exception.getMessage();
    }

    private int normalizePage(int page) {
        return page < 1 ? 1 : page;
    }

    private int normalizePageSize(int pageSize, int maxPageSize) {
        if (pageSize < 1) {
            return Math.min(50, maxPageSize);
        }
        return Math.min(pageSize, maxPageSize);
    }

    private Path normalizeOutputDirectory(String outputDirectory) {
        if (outputDirectory == null || outputDirectory.isBlank()) {
            return Path.of("exports");
        }
        return Path.of(outputDirectory.trim());
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

