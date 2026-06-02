package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.AuthResponse;
import com.mycompany.perpustakaan.api.LibraryApi;
import com.mycompany.perpustakaan.api.LoanManagementPage;
import com.mycompany.perpustakaan.api.LoanResponse;
import com.mycompany.perpustakaan.api.LoanSummary;

public class TestStaffLoansReturns {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "list";
        String username = args.length > 1 ? args[1] : "staff01";
        String password = args.length > 2 ? args[2] : "password";

        LibraryApi libraryApi = new LibraryApi();

        try {
            AuthResponse authResponse = libraryApi.login(username, password);
            if (!authResponse.isSuccess()) {
                System.out.println("Login gagal: " + authResponse.getMessage());
                return;
            }

            System.out.println("Login berhasil: " + authResponse.getUser().getUsername() + " | role=" + authResponse.getUser().getRole());

            if ("create-return".equalsIgnoreCase(mode)) {
                int idUser = args.length > 3 ? Integer.parseInt(args[3]) : 4;
                int idBuku = args.length > 4 ? Integer.parseInt(args[4]) : 2;
                int loanDays = args.length > 5 ? Integer.parseInt(args[5]) : 7;

                LoanResponse createResponse = libraryApi.createLoanForUser(idUser, idBuku, loanDays);
                printLoanResponse("PROSES PEMINJAMAN", createResponse);

                if (createResponse.isSuccess()) {
                    LoanResponse returnResponse = libraryApi.processReturn(createResponse.getLoan().getIdPeminjaman());
                    printLoanResponse("PROSES PENGEMBALIAN", returnResponse);
                }
            } else if ("return".equalsIgnoreCase(mode)) {
                int idPeminjaman = args.length > 3 ? Integer.parseInt(args[3]) : 1;
                printLoanResponse("PROSES PENGEMBALIAN", libraryApi.processReturn(idPeminjaman));
            } else {
                System.out.println("Mode aman: hanya menampilkan data peminjaman.");
                System.out.println("Mode mutasi:");
                System.out.println("- TestStaffLoansReturns create-return <staff> <password> <id_user> <id_buku> <hari_pinjam>");
                System.out.println("- TestStaffLoansReturns return <staff> <password> <id_peminjaman>");
            }

            printManagementPage("SEMUA PEMINJAMAN", libraryApi.getLoansForManagement(null, 1, 10));
            printManagementPage("PEMINJAMAN AKTIF", libraryApi.getLoansForManagement("aktif", 1, 10));
            libraryApi.logout();
        } catch (Exception exception) {
            System.out.println("Test Staff Loans & Returns gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printLoanResponse(String title, LoanResponse response) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Status response: " + response.isSuccess());
        System.out.println("Pesan          : " + response.getMessage());
        if (response.getLoan() != null) {
            printLoan(response.getLoan());
        }
    }

    private static void printManagementPage(String title, LoanManagementPage page) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Status     : " + page.getStatus());
        System.out.println("Page       : " + page.getPage() + "/" + page.getTotalPages());
        System.out.println("Page Size  : " + page.getPageSize());
        System.out.println("Total Data : " + page.getTotalItems());

        if (page.getLoans().isEmpty()) {
            System.out.println("Tidak ada data peminjaman.");
            return;
        }

        int number = 1;
        for (LoanSummary loan : page.getLoans()) {
            System.out.println(number + ".");
            printLoan(loan);
            number++;
        }
    }

    private static void printLoan(LoanSummary loan) {
        System.out.println("ID Peminjaman : " + loan.getIdPeminjaman());
        System.out.println("ID Buku       : " + loan.getIdBuku());
        System.out.println("Kode Buku     : " + loan.getKodeBuku());
        System.out.println("Judul         : " + loan.getJudulBuku());
        System.out.println("Tanggal Pinjam: " + loan.getTanggalPinjam());
        System.out.println("Jatuh Tempo   : " + loan.getTanggalJatuhTempo());
        System.out.println("Tanggal Kembali: " + loan.getTanggalKembali());
        System.out.println("Status        : " + loan.getStatus());
        System.out.println("Hari Terlambat: " + loan.getHariTerlambat());
        System.out.println("Denda         : " + loan.getDendaBerjalan());
    }
}
