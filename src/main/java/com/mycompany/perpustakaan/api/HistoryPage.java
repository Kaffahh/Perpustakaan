package com.mycompany.perpustakaan.api;

import java.util.List;

public class HistoryPage {

    private final List<LoanSummary> loans;
    private final int totalItems;
    private final int page;
    private final int pageSize;
    private final int totalPages;
    private final String status;

    public HistoryPage(List<LoanSummary> loans, int totalItems, int page, int pageSize, String status) {
        this.loans = loans;
        this.totalItems = totalItems;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages(totalItems, pageSize);
        this.status = status;
    }

    public List<LoanSummary> getLoans() {
        return loans;
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

    public String getStatus() {
        return status;
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
