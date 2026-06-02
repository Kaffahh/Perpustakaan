package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.AdminDashboardSummary;
import com.mycompany.perpustakaan.api.AuthResponse;
import com.mycompany.perpustakaan.api.InventoryReportRow;
import com.mycompany.perpustakaan.api.LibraryApi;
import com.mycompany.perpustakaan.api.LoanReportRow;
import com.mycompany.perpustakaan.api.ReportExportResponse;
import java.time.LocalDate;
import java.util.List;

public class TestAdminDashboardReports {

    public static void main(String[] args) {
        String username = args.length > 0 ? args[0] : "admin";
        String password = args.length > 1 ? args[1] : "password";
        String outputDirectory = args.length > 2 ? args[2] : "exports";

        LibraryApi libraryApi = new LibraryApi();

        try {
            AuthResponse authResponse = libraryApi.login(username, password);
            if (!authResponse.isSuccess()) {
                System.out.println("Login gagal: " + authResponse.getMessage());
                return;
            }

            System.out.println("Login berhasil: " + authResponse.getUser().getUsername() + " | role=" + authResponse.getUser().getRole());

            AdminDashboardSummary summary = libraryApi.getAdminDashboardSummary();
            printSummary(summary);

            List<InventoryReportRow> inventoryRows = libraryApi.getInventoryReport();
            System.out.println("Jumlah baris inventory: " + inventoryRows.size());

            List<LoanReportRow> loanRows = libraryApi.getLoanReport(null, LocalDate.now());
            System.out.println("Jumlah baris peminjaman: " + loanRows.size());

            ReportExportResponse inventoryPdf = libraryApi.exportInventoryReport("pdf", outputDirectory);
            printExportResponse("EXPORT INVENTORY PDF", inventoryPdf);

            ReportExportResponse loansXlsx = libraryApi.exportLoanReport("xlsx", outputDirectory, null, LocalDate.now());
            printExportResponse("EXPORT PEMINJAMAN XLSX", loansXlsx);

            libraryApi.logout();
        } catch (Exception exception) {
            System.out.println("Test Admin Dashboard & Reports gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printSummary(AdminDashboardSummary summary) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println("ADMIN DASHBOARD SUMMARY");
        System.out.println("============================================================");
        System.out.println("Total buku              : " + summary.getTotalBuku());
        System.out.println("Total anggota           : " + summary.getTotalAnggota());
        System.out.println("Total peminjaman aktif  : " + summary.getTotalPeminjamanAktif());
        System.out.println("Total denda             : " + summary.getTotalDenda());
    }

    private static void printExportResponse(String title, ReportExportResponse response) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Status response: " + response.isSuccess());
        System.out.println("Pesan          : " + response.getMessage());
        System.out.println("Format         : " + response.getFormat());
        System.out.println("File path      : " + response.getFilePath());
    }
}
