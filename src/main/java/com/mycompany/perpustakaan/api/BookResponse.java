package com.mycompany.perpustakaan.api;

public class BookResponse {

    private final boolean success;
    private final String message;
    private final BookSummary book;

    public BookResponse(boolean success, String message, BookSummary book) {
        this.success = success;
        this.message = ApiResponseMessages.normalize(success, message);
        this.book = book;
    }

    public static BookResponse success(String message, BookSummary book) {
        return new BookResponse(true, message, book);
    }

    public static BookResponse failure(String message) {
        return new BookResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public BookSummary getBook() {
        return book;
    }
}
