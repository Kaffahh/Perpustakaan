package com.mycompany.perpustakaan.api;

public class PageMeta {

    private final int totalItems;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    public PageMeta(int totalItems, int page, int pageSize) {
        this.totalItems = Math.max(0, totalItems);
        this.page = Math.max(1, page);
        this.pageSize = Math.max(1, pageSize);
        this.totalPages = calculateTotalPages(this.totalItems, this.pageSize);
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
