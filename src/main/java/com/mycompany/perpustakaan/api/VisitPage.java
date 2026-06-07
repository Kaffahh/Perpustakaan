package com.mycompany.perpustakaan.api;

import java.util.List;

public class VisitPage {

    private final List<VisitSummary> visits;
    private final PageMeta pageMeta;
    private final String keyword;
    private final String status;

    public VisitPage(List<VisitSummary> visits, int totalItems, int page, int pageSize, String keyword, String status) {
        this.visits = visits;
        this.pageMeta = new PageMeta(totalItems, page, pageSize);
        this.keyword = keyword;
        this.status = status;
    }

    public List<VisitSummary> getVisits() {
        return visits;
    }

    public int getTotalItems() {
        return pageMeta.getTotalItems();
    }

    public int getPage() {
        return pageMeta.getPage();
    }

    public int getPageSize() {
        return pageMeta.getPageSize();
    }

    public int getTotalPages() {
        return pageMeta.getTotalPages();
    }

    public boolean hasNextPage() {
        return pageMeta.hasNextPage();
    }

    public boolean hasPreviousPage() {
        return pageMeta.hasPreviousPage();
    }

    public String getKeyword() {
        return keyword;
    }

    public String getStatus() {
        return status;
    }
}
