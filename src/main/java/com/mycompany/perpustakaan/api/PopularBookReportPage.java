package com.mycompany.perpustakaan.api;

import java.util.List;

public class PopularBookReportPage {

    private final List<PopularBookReportRow> rows;
    private final PageMeta pageMeta;

    public PopularBookReportPage(List<PopularBookReportRow> rows, int totalItems, int page, int pageSize) {
        this.rows = rows;
        this.pageMeta = new PageMeta(totalItems, page, pageSize);
    }

    public List<PopularBookReportRow> getRows() {
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
}
