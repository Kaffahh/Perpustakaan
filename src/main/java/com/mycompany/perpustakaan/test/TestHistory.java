package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.AuthResponse;
import com.mycompany.perpustakaan.api.HistoryPage;
import com.mycompany.perpustakaan.api.LibraryApi;
import com.mycompany.perpustakaan.api.LoanSummary;

public class TestHistory {

    public static void main(String[] args) {
        String username = args.length > 0 ? args[0] : "mhs2021001";
        String password = args.length > 1 ? args[1] : "password";
        int page = args.length > 2 ? Integer.parseInt(args[2]) : 1;
        int pageSize = args.length > 3 ? Integer.parseInt(args[3]) : 5;

        LibraryApi libraryApi = new LibraryApi();

        try {
            AuthResponse authResponse = libraryApi.login(username, password);
            if (!authResponse.isSuccess()) {
                System.out.println("Login gagal: " + authResponse.getMessage());
                return;
            }

            System.out.println("Login berhasil: " + authResponse.getUser().getUsername());

            printHistory("SEMUA HISTORY", libraryApi.getLoanHistory(null, page, pageSize));
            printHistory("HISTORY DIPINJAM", libraryApi.getLoanHistory("dipinjam", 1, pageSize));
            printHistory("HISTORY DIKEMBALIKAN", libraryApi.getLoanHistory("dikembalikan", 1, pageSize));

            libraryApi.logout();
        } catch (Exception exception) {
            System.out.println("Test History gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printHistory(String title, HistoryPage historyPage) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Status     : " + historyPage.getStatus());
        System.out.println("Page       : " + historyPage.getPage() + "/" + historyPage.getTotalPages());
        System.out.println("Page Size  : " + historyPage.getPageSize());
        System.out.println("Total Data : " + historyPage.getTotalItems());
        System.out.println("Has Prev   : " + historyPage.hasPreviousPage());
        System.out.println("Has Next   : " + historyPage.hasNextPage());

        if (historyPage.getLoans().isEmpty()) {
            System.out.println("Belum ada data history.");
            return;
        }

        int number = 1;
        for (LoanSummary loan : historyPage.getLoans()) {
            System.out.println(number + ". " + loan.getJudulBuku());
            System.out.println("   ID Peminjaman : " + loan.getIdPeminjaman());
            System.out.println("   Kode Buku     : " + loan.getKodeBuku());
            System.out.println("   Tanggal Pinjam: " + loan.getTanggalPinjam());
            System.out.println("   Jatuh Tempo   : " + loan.getTanggalJatuhTempo());
            System.out.println("   Tanggal Kembali: " + loan.getTanggalKembali());
            System.out.println("   Status        : " + loan.getStatus());
            System.out.println("   Hari Terlambat: " + loan.getHariTerlambat());
            System.out.println("   Denda         : " + loan.getDendaBerjalan());
            number++;
        }
    }
}
