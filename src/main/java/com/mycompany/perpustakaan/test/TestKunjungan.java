package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.AuthResponse;
import com.mycompany.perpustakaan.api.LibraryApi;
import com.mycompany.perpustakaan.api.VisitResponse;
import com.mycompany.perpustakaan.api.VisitSummary;

public class TestKunjungan {

    public static void main(String[] args) {
        String username = args.length > 0 ? args[0] : "mhs2021001";
        String password = args.length > 1 ? args[1] : "password";

        LibraryApi libraryApi = new LibraryApi();

        try {
            AuthResponse authResponse = libraryApi.login(username, password);
            if (!authResponse.isSuccess()) {
                System.out.println("Login gagal: " + authResponse.getMessage());
                return;
            }

            System.out.println("Login berhasil: " + authResponse.getUser().getUsername());

            VisitResponse registeredVisit = libraryApi.addRegisteredUserVisit(
                    "mahasiswa",
                    "Universitas Contoh",
                    "Membaca buku dan belajar");
            printVisitResponse("KUNJUNGAN USER TERDAFTAR", registeredVisit);

            if (registeredVisit.isSuccess()) {
                VisitResponse finishedVisit = libraryApi.finishVisit(registeredVisit.getVisit().getIdKunjungan());
                printVisitResponse("UPDATE STATUS SELESAI", finishedVisit);
            }

            VisitResponse guestVisit = libraryApi.addGuestVisit(
                    "Tamu Demo",
                    "umum",
                    "Masyarakat Umum",
                    "Mencari referensi");
            printVisitResponse("KUNJUNGAN TAMU", guestVisit);

            if (guestVisit.isSuccess()) {
                VisitResponse cancelledVisit = libraryApi.cancelVisit(guestVisit.getVisit().getIdKunjungan());
                printVisitResponse("UPDATE STATUS BATAL", cancelledVisit);
            }

            libraryApi.logout();
        } catch (Exception exception) {
            System.out.println("Test Kunjungan gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printVisitResponse(String title, VisitResponse response) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Status response: " + response.isSuccess());
        System.out.println("Pesan          : " + response.getMessage());

        if (response.getVisit() == null) {
            return;
        }

        printVisit(response.getVisit());
    }

    private static void printVisit(VisitSummary visit) {
        System.out.println("ID Kunjungan   : " + visit.getIdKunjungan());
        System.out.println("ID User        : " + visit.getIdUser());
        System.out.println("Nama           : " + visit.getNamaPengunjung());
        System.out.println("Jenis          : " + visit.getJenisPengunjung());
        System.out.println("Asal Instansi  : " + visit.getAsalInstansi());
        System.out.println("Keperluan      : " + visit.getKeperluan());
        System.out.println("Status         : " + visit.getStatusKunjungan());
        System.out.println("Tanggal        : " + visit.getTanggalKunjungan());
    }
}
