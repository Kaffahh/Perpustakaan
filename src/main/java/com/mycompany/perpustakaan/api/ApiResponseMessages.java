package com.mycompany.perpustakaan.api;

final class ApiResponseMessages {

    private ApiResponseMessages() {
    }

    static String normalize(boolean success, String message) {
        if (message == null || message.isBlank()) {
            return success ? "Operasi berhasil." : "Operasi gagal.";
        }
        return message;
    }
}
