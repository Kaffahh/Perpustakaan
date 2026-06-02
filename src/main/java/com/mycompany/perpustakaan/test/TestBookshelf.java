package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.BookSummary;
import com.mycompany.perpustakaan.api.BookshelfPage;
import com.mycompany.perpustakaan.api.LibraryApi;
import java.util.List;

public class TestBookshelf {

    public static void main(String[] args) {
        LibraryApi libraryApi = new LibraryApi();

        try {
            printCategories(libraryApi);

            printPage("SEMUA BUKU - HALAMAN 1", libraryApi.getBookshelfPage(null, null, 1, 5));
            printPage("SEARCH BUKU", libraryApi.getBookshelfPage("Algoritma", null, 1, 5));

            List<String> categories = libraryApi.getBookCategories();
            if (!categories.isEmpty()) {
                String firstCategory = categories.get(0);
                printPage("FILTER KATEGORI: " + firstCategory, libraryApi.getBookshelfPage(null, firstCategory, 1, 5));
            }
        } catch (Exception exception) {
            System.out.println("Test Bookshelf gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printCategories(LibraryApi libraryApi) throws Exception {
        List<String> categories = libraryApi.getBookCategories();
        System.out.println("Kategori tersedia: " + categories.size());
        for (String category : categories) {
            System.out.println("- " + category);
        }
    }

    private static void printPage(String title, BookshelfPage page) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Page       : " + page.getPage() + "/" + page.getTotalPages());
        System.out.println("Page Size  : " + page.getPageSize());
        System.out.println("Total Data : " + page.getTotalItems());
        System.out.println("Keyword    : " + page.getKeyword());
        System.out.println("Kategori   : " + page.getKategori());
        System.out.println("Has Prev   : " + page.hasPreviousPage());
        System.out.println("Has Next   : " + page.hasNextPage());

        if (page.getBooks().isEmpty()) {
            System.out.println("Tidak ada buku.");
            return;
        }

        int number = 1;
        for (BookSummary book : page.getBooks()) {
            System.out.println(number + ". " + book.getJudul());
            System.out.println("   Kode     : " + book.getKodeBuku());
            System.out.println("   Penulis  : " + book.getPenulis());
            System.out.println("   Kategori : " + book.getKategori());
            System.out.println("   Stok     : " + book.getStokTersedia() + "/" + book.getStokTotal());
            number++;
        }
    }
}
