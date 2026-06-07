package com.mycompany.perpustakaan.api;

public class VisitResponse {

    private final boolean success;
    private final String message;
    private final VisitSummary visit;

    public VisitResponse(boolean success, String message, VisitSummary visit) {
        this.success = success;
        this.message = ApiResponseMessages.normalize(success, message);
        this.visit = visit;
    }

    public static VisitResponse success(String message, VisitSummary visit) {
        return new VisitResponse(true, message, visit);
    }

    public static VisitResponse failure(String message) {
        return new VisitResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public VisitSummary getVisit() {
        return visit;
    }
}
