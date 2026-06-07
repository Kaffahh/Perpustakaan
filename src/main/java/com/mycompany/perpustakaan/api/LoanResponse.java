package com.mycompany.perpustakaan.api;

public class LoanResponse {

    private final boolean success;
    private final String message;
    private final LoanSummary loan;

    public LoanResponse(boolean success, String message, LoanSummary loan) {
        this.success = success;
        this.message = ApiResponseMessages.normalize(success, message);
        this.loan = loan;
    }

    public static LoanResponse success(String message, LoanSummary loan) {
        return new LoanResponse(true, message, loan);
    }

    public static LoanResponse failure(String message) {
        return new LoanResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public LoanSummary getLoan() {
        return loan;
    }
}
