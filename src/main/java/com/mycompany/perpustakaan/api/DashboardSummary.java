package com.mycompany.perpustakaan.api;

import java.util.List;

public class DashboardSummary {

    private final UserSummary profile;
    private final int totalBooks;
    private final List<BookSummary> latestBooks;

    public DashboardSummary(UserSummary profile, int totalBooks, List<BookSummary> latestBooks) {
        this.profile = profile;
        this.totalBooks = totalBooks;
        this.latestBooks = latestBooks;
    }

    public UserSummary getProfile() {
        return profile;
    }

    public int getTotalBooks() {
        return totalBooks;
    }

    public List<BookSummary> getLatestBooks() {
        return latestBooks;
    }
}