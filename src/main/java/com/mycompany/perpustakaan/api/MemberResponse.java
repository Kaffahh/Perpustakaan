package com.mycompany.perpustakaan.api;

public class MemberResponse {

    private final boolean success;
    private final String message;
    private final MemberSummary member;

    public MemberResponse(boolean success, String message, MemberSummary member) {
        this.success = success;
        this.message = ApiResponseMessages.normalize(success, message);
        this.member = member;
    }

    public static MemberResponse success(String message, MemberSummary member) {
        return new MemberResponse(true, message, member);
    }

    public static MemberResponse failure(String message) {
        return new MemberResponse(false, message, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public MemberSummary getMember() {
        return member;
    }
}
