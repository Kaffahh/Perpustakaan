package com.mycompany.perpustakaan.api;

import java.util.List;

public class BookshelfPage {

    private final List<BookSummary> books;
    private final int totalItems;
    private final int page;
    private final int pageSize;
    private final int totalPages;
    private final String keyword;
    private final String kategori;

    public BookshelfPage(List<BookSummary> books, int totalItems, int page, int pageSize, String keyword, String kategori) {
        this.books = books;
        this.totalItems = totalItems;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages(totalItems, pageSize);
        this.keyword = keyword;
        this.kategori = kategori;
    }

    public List<BookSummary> getBooks() {
        return books;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String getKeyword() {
        return keyword;
    }

    public String getKategori() {
        return kategori;
    }

    public boolean hasNextPage() {
        return page < totalPages;
    }

    public boolean hasPreviousPage() {
        return page > 1 && totalPages > 0;
    }

    private int calculateTotalPages(int totalItems, int pageSize) {
        if (totalItems <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }
}
