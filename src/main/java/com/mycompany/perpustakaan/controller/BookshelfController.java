package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.dao.BukuDao;
import com.mycompany.perpustakaan.model.Buku;
import java.sql.SQLException;
import java.util.List;

public class BookshelfController {

    private static final int MAX_PAGE_SIZE = 100;

    private final BukuDao bukuDao;

    public BookshelfController() {
        this.bukuDao = new BukuDao();
    }

    public List<Buku> getBooks(String keyword, String kategori, int page, int pageSize) throws SQLException {
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;

        return bukuDao.findBooks(normalizeText(keyword), normalizeText(kategori), safePageSize, offset);
    }

    public int countBooks(String keyword, String kategori) throws SQLException {
        return bukuDao.countBooks(normalizeText(keyword), normalizeText(kategori));
    }

    public List<String> getCategories() throws SQLException {
        return bukuDao.findCategories();
    }

    public int normalizePage(int page) {
        if (page < 1) {
            return 1;
        }
        return page;
    }

    public int normalizePageSize(int pageSize) {
        if (pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
