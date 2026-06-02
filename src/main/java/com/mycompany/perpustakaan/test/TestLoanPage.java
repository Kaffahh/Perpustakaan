package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.AuthResponse;
import com.mycompany.perpustakaan.api.LibraryApi;
import com.mycompany.perpustakaan.api.LoanResponse;
import com.mycompany.perpustakaan.api.LoanSummary;
import java.util.List;

public class TestLoanPage {

    public static void main(String[] args) {
        String username = args.length > 1 ? args[1] : "mhs2021001";
        String password = args.length > 2 ? args[2] : "password";
        boolean shouldBorrow = args.length > 0 && "borrow".equalsIgnoreCase(args[0]);
        int idBuku = args.length > 3 ? Integer.parseInt(args[3]) : 1;
        int loanDays = args.length > 4 ? Integer.parseInt(args[4]) : 7;

        LibraryApi libraryApi = new LibraryApi();

        try {
            AuthResponse authResponse = libraryApi.login(username, password);
            if (!authResponse.isSuccess()) {
                System.out.println("Login gagal: " + authResponse.getMessage());
                return;
            }

            System.out.println("Login berhasil: " + authResponse.getUser().getUsername());

            if (shouldBorrow) {
                LoanResponse loanResponse = libraryApi.requestLoan(idBuku, loanDays);
                System.out.println("Ajukan peminjaman: " + loanResponse.getMessage());
                if (loanResponse.isSuccess()) {
                    printLoan(loanResponse.getLoan());
                }
            } else {
                System.out.println("Mode aman: tidak membuat peminjaman baru.");
                System.out.println("Untuk test pinjam: TestLoanPage borrow <username> <password> <id_buku> <hari_pinjam>");
            }

            printCurrentLoans(libraryApi);
            libraryApi.logout();
        } catch (Exception exception) {
            System.out.println("Test Loan Page gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printCurrentLoans(LibraryApi libraryApi) throws Exception {
        List<LoanSummary> currentLoans = libraryApi.getCurrentLoans();

        System.out.println();
        System.out.println("============================================================");
        System.out.println("PINJAMAN AKTIF");
        System.out.println("============================================================");
        System.out.println("Total pinjaman aktif: " + currentLoans.size());

        if (currentLoans.isEmpty()) {
            System.out.println("Tidak ada buku yang sedang dipinjam.");
            return;
        }

        for (LoanSummary loan : currentLoans) {
            printLoan(loan);
        }
    }

    private static void printLoan(LoanSummary loan) {
        System.out.println("- ID Peminjaman : " + loan.getIdPeminjaman());
        System.out.println("  Buku          : " + loan.getJudulBuku());
        System.out.println("  Kode          : " + loan.getKodeBuku());
        System.out.println("  Penulis       : " + loan.getPenulisBuku());
        System.out.println("  Kategori      : " + loan.getKategoriBuku());
        System.out.println("  Tanggal Pinjam: " + loan.getTanggalPinjam());
        System.out.println("  Jatuh Tempo   : " + loan.getTanggalJatuhTempo());
        System.out.println("  Status        : " + loan.getStatus());
        System.out.println("  Hari Terlambat: " + loan.getHariTerlambat());
        System.out.println("  Denda Berjalan: " + loan.getDendaBerjalan());
    }
}
