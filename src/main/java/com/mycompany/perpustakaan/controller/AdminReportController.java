package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.api.AdminDashboardSummary;
import com.mycompany.perpustakaan.api.InventoryReportRow;
import com.mycompany.perpustakaan.api.LoanReportRow;
import com.mycompany.perpustakaan.api.PopularBookReportRow;
import com.mycompany.perpustakaan.api.VisitReportRow;
import com.mycompany.perpustakaan.dao.ReportDao;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.ReportExporter;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class AdminReportController {

    private final ReportDao reportDao;
    private final ReportExporter reportExporter;

    public AdminReportController() {
        this.reportDao = new ReportDao();
        this.reportExporter = new ReportExporter();
    }

    public AdminDashboardSummary getAdminDashboardSummary() throws SQLException {
        requireAdmin();
        int totalBooks = reportDao.countBooks();
        int totalMembers = reportDao.countMembers();
        int totalActiveLoans = reportDao.countActiveLoans();
        BigDecimal totalFines = reportDao.sumFines();
        return new AdminDashboardSummary(totalBooks, totalMembers, totalActiveLoans, totalFines);
    }

    public List<InventoryReportRow> getInventoryReport() throws SQLException {
        requireAdmin();
        return reportDao.getInventoryReport();
    }

    public List<InventoryReportRow> getInventoryReport(String keyword, String kategori, int page, int pageSize) throws SQLException {
        requireAdmin();
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize < 1 ? 50 : Math.min(pageSize, 200);
        return reportDao.getInventoryReport(normalizeOptionalText(keyword), normalizeOptionalText(kategori), safePageSize, (safePage - 1) * safePageSize);
    }

    public int countInventoryReport(String keyword, String kategori) throws SQLException {
        requireAdmin();
        return reportDao.countInventoryReport(normalizeOptionalText(keyword), normalizeOptionalText(kategori));
    }

    public List<LoanReportRow> getLoanReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        requireAdmin();
        validateDateRange(startDate, endDate);
        return reportDao.getLoanReport(startDate, endDate);
    }

    public List<PopularBookReportRow> getPopularBookReport(int limit) throws SQLException {
        requireAdmin();
        return reportDao.getPopularBookReport(normalizeLimit(limit));
    }

    public List<PopularBookReportRow> getPopularBookReport(int page, int pageSize) throws SQLException {
        requireAdmin();
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize < 1 ? 25 : Math.min(pageSize, 100);
        return reportDao.getPopularBookReport(safePage, safePageSize);
    }

    public int countPopularBookReport() throws SQLException {
        requireAdmin();
        return reportDao.countPopularBookReport();
    }

    public List<VisitReportRow> getVisitReport(String keyword, String status) throws SQLException {
        requireAdmin();
        return reportDao.getVisitReport(normalizeOptionalText(keyword), normalizeOptionalText(status));
    }

    public List<VisitReportRow> getVisitReport(String keyword, String status, int page, int pageSize) throws SQLException {
        requireAdmin();
        int safePage = page < 1 ? 1 : page;
        int safePageSize = pageSize < 1 ? 50 : Math.min(pageSize, 200);
        return reportDao.getVisitReport(normalizeOptionalText(keyword), normalizeOptionalText(status), safePageSize, (safePage - 1) * safePageSize);
    }

    public int countVisitReport(String keyword, String status) throws SQLException {
        requireAdmin();
        return reportDao.countVisitReport(normalizeOptionalText(keyword), normalizeOptionalText(status));
    }

    public Path exportInventoryReport(String format, String outputDirectory) throws SQLException, IOException {
        requireAdmin();
        String safeFormat = normalizeFormat(format);
        Path safeOutputDirectory = normalizeOutputDirectory(outputDirectory);
        return reportExporter.exportInventory(reportDao.getInventoryReport(), safeFormat, safeOutputDirectory);
    }

    public Path exportLoanReport(String format, String outputDirectory, LocalDate startDate, LocalDate endDate) throws SQLException, IOException {
        requireAdmin();
        validateDateRange(startDate, endDate);
        String safeFormat = normalizeFormat(format);
        Path safeOutputDirectory = normalizeOutputDirectory(outputDirectory);
        return reportExporter.exportLoans(reportDao.getLoanReport(startDate, endDate), safeFormat, safeOutputDirectory);
    }

    public Path exportVisitReport(String format, String outputDirectory, String keyword, String status) throws SQLException, IOException {
        requireAdmin();
        String safeFormat = normalizeFormat(format);
        Path safeOutputDirectory = normalizeOutputDirectory(outputDirectory);
        return reportExporter.exportVisits(reportDao.getVisitReport(normalizeOptionalText(keyword), normalizeOptionalText(status)), safeFormat, safeOutputDirectory);
    }

    public String normalizeFormat(String format) {
        if (format == null || format.isBlank()) {
            throw new IllegalArgumentException("Format export wajib diisi.");
        }

        String normalizedFormat = format.trim().toLowerCase();
        if ("xslx".equals(normalizedFormat)) {
            normalizedFormat = "xlsx";
        }
        if (!"pdf".equals(normalizedFormat) && !"xlsx".equals(normalizedFormat)) {
            throw new IllegalArgumentException("Format export hanya boleh pdf atau xlsx.");
        }
        return normalizedFormat;
    }

    private User requireAdmin() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User harus login sebelum mengakses admin dashboard & reports.");
        }
        if (!currentUser.isAdmin()) {
            throw new IllegalStateException("Hanya admin yang boleh mengakses admin dashboard & reports.");
        }
        return currentUser;
    }

    private Path normalizeOutputDirectory(String outputDirectory) {
        if (outputDirectory == null || outputDirectory.isBlank()) {
            return Path.of("exports");
        }
        return Path.of(outputDirectory.trim());
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Tanggal awal tidak boleh lebih besar dari tanggal akhir.");
        }
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return 10;
        }
        return Math.min(limit, 100);
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank() || "semua".equalsIgnoreCase(value)) {
            return null;
        }
        return value.trim();
    }
}
