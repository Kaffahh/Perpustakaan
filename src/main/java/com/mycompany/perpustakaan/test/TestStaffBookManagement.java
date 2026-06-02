package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.AuthResponse;
import com.mycompany.perpustakaan.api.BookRequest;
import com.mycompany.perpustakaan.api.BookResponse;
import com.mycompany.perpustakaan.api.BookSummary;
import com.mycompany.perpustakaan.api.LibraryApi;

public class TestStaffBookManagement {

    public static void main(String[] args) {
        String username = args.length > 0 ? args[0] : "staff01";
        String password = args.length > 1 ? args[1] : "password";
        String uniqueCode = "TMP-" + (System.currentTimeMillis() % 1000000);

        LibraryApi libraryApi = new LibraryApi();

        try {
            AuthResponse authResponse = libraryApi.login(username, password);
            if (!authResponse.isSuccess()) {
                System.out.println("Login gagal: " + authResponse.getMessage());
                return;
            }

            System.out.println("Login berhasil: " + authResponse.getUser().getUsername() + " | role=" + authResponse.getUser().getRole());

            BookRequest createRequest = new BookRequest(uniqueCode, "Buku Demo Staff", "Backend Tester", "Codex Press", "Demo", 2026, 3, 5);
            BookResponse createResponse = libraryApi.addBook(createRequest);
            printBookResponse("TAMBAH BUKU", createResponse);
            if (!createResponse.isSuccess()) {
                return;
            }

            int idBuku = createResponse.getBook().getIdBuku();
            BookRequest updateRequest = new BookRequest(uniqueCode, "Buku Demo Staff Updated", "Backend Tester", "Codex Press", "Demo Updated", 2026, 4, 6);

            printBookResponse("UPDATE BUKU", libraryApi.updateBook(idBuku, updateRequest));
            printBookResponse("UPDATE STOK", libraryApi.updateBookStock(idBuku, 2, 6));

            System.out.println();
            System.out.println("DETAIL SETELAH UPDATE");
            printBook(libraryApi.getBookByIdForManagement(idBuku));

            printBookResponse("HAPUS BUKU", libraryApi.deleteBook(idBuku));
            libraryApi.logout();
        } catch (Exception exception) {
            System.out.println("Test Staff Book Management gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printBookResponse(String title, BookResponse response) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Status response: " + response.isSuccess());
        System.out.println("Pesan          : " + response.getMessage());
        if (response.getBook() != null) {
            printBook(response.getBook());
        }
    }

    private static void printBook(BookSummary book) {
        System.out.println("ID Buku        : " + book.getIdBuku());
        System.out.println("Kode Buku      : " + book.getKodeBuku());
        System.out.println("Judul          : " + book.getJudul());
        System.out.println("Penulis        : " + book.getPenulis());
        System.out.println("Penerbit       : " + book.getPenerbit());
        System.out.println("Kategori       : " + book.getKategori());
        System.out.println("Tahun Terbit   : " + book.getTahunTerbit());
        System.out.println("Stok           : " + book.getStokTersedia() + "/" + book.getStokTotal());
        System.out.println("Created By     : " + book.getCreatedBy());
    }
}
