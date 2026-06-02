package com.mycompany.perpustakaan.api;

public class ReportExportResponse {

    private final boolean success;
    private final String message;
    private final String filePath;
    private final String format;

    public ReportExportResponse(boolean success, String message, String filePath, String format) {
        this.success = success;
        this.message = message;
        this.filePath = filePath;
        this.format = format;
    }

    public static ReportExportResponse success(String message, String filePath, String format) {
        return new ReportExportResponse(true, message, filePath, format);
    }

    public static ReportExportResponse failure(String message) {
        return new ReportExportResponse(false, message, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFormat() {
        return format;
    }
}
