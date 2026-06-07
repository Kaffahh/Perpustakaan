package com.mycompany.perpustakaan.api;

import java.util.List;

public class InventoryReportPage {

    private final List<InventoryReportRow> rows;
    private final PageMeta pageMeta;
    private final String keyword;
    private final String kategori;

    public InventoryReportPage(List<InventoryReportRow> rows, int totalItems, int page, int pageSize, String keyword, String kategori) {
        this.rows = rows;
        this.pageMeta = new PageMeta(totalItems, page, pageSize);
        this.keyword = keyword;
        this.kategori = kategori;
    }

    public List<InventoryReportRow> getRows() {
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

    public String getKategori() {
        return kategori;
    }
}
