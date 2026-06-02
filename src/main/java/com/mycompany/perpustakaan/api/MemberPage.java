package com.mycompany.perpustakaan.api;

import java.util.List;

public class MemberPage {

    private final List<MemberSummary> members;
    private final int totalItems;
    private final int page;
    private final int pageSize;
    private final int totalPages;
    private final String keyword;
    private final String statusAkun;

    public MemberPage(List<MemberSummary> members, int totalItems, int page, int pageSize, String keyword, String statusAkun) {
        this.members = members;
        this.totalItems = totalItems;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = calculateTotalPages(totalItems, pageSize);
        this.keyword = keyword;
        this.statusAkun = statusAkun;
    }

    public List<MemberSummary> getMembers() {
        return members;
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

    public String getStatusAkun() {
        return statusAkun;
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
