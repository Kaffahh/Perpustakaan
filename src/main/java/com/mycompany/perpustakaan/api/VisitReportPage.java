package com.mycompany.perpustakaan.api;

import java.util.List;

public class VisitReportPage {

    private final List<VisitReportRow> rows;
    private final PageMeta pageMeta;
    private final String keyword;
    private final String status;

    public VisitReportPage(List<VisitReportRow> rows, int totalItems, int page, int pageSize, String keyword, String status) {
        this.rows = rows;
        this.pageMeta = new PageMeta(totalItems, page, pageSize);
        this.keyword = keyword;
        this.status = status;
    }

    public List<VisitReportRow> getRows() {
        return rows;
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
