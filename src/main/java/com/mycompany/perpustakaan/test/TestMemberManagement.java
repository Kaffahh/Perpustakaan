package com.mycompany.perpustakaan.test;

import com.mycompany.perpustakaan.api.AuthResponse;
import com.mycompany.perpustakaan.api.LibraryApi;
import com.mycompany.perpustakaan.api.MemberPage;
import com.mycompany.perpustakaan.api.MemberRequest;
import com.mycompany.perpustakaan.api.MemberResponse;
import com.mycompany.perpustakaan.api.MemberSummary;

public class TestMemberManagement {

    public static void main(String[] args) {
        String username = args.length > 0 ? args[0] : "staff01";
        String password = args.length > 1 ? args[1] : "password";
        String uniqueCode = String.valueOf(System.currentTimeMillis() % 1000000);
        String memberUsername = "tmpmember" + uniqueCode;

        LibraryApi libraryApi = new LibraryApi();

        try {
            AuthResponse authResponse = libraryApi.login(username, password);
            if (!authResponse.isSuccess()) {
                System.out.println("Login gagal: " + authResponse.getMessage());
                return;
            }

            System.out.println("Login berhasil: " + authResponse.getUser().getUsername() + " | role=" + authResponse.getUser().getRole());

            MemberRequest createRequest = new MemberRequest(
                    memberUsername,
                    "Member Demo " + uniqueCode,
                    memberUsername + "@demo.test",
                    "password");
            MemberResponse createResponse = libraryApi.addMember(createRequest);
            printMemberResponse("TAMBAH ANGGOTA", createResponse);
            if (!createResponse.isSuccess()) {
                return;
            }

            int idUser = createResponse.getMember().getIdUser();
            MemberRequest updateRequest = new MemberRequest(
                    memberUsername,
                    "Member Demo Updated " + uniqueCode,
                    memberUsername + "@demo.test",
                    null);

            printMemberResponse("EDIT ANGGOTA", libraryApi.updateMember(idUser, updateRequest));
            printMemberResponse("SUSPEND ANGGOTA", libraryApi.suspendMember(idUser));
            printMemberResponse("AKTIFKAN ANGGOTA", libraryApi.activateMember(idUser));

            MemberPage page = libraryApi.searchMembers(memberUsername, "aktif", 1, 10);
            printMemberPage("CARI ANGGOTA", page);

            printMemberResponse("HAPUS ANGGOTA", libraryApi.deleteMember(idUser));
            libraryApi.logout();
        } catch (Exception exception) {
            System.out.println("Test Member Management gagal.");
            System.out.println("Pesan error: " + exception.getMessage());
        }
    }

    private static void printMemberResponse(String title, MemberResponse response) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Status response: " + response.isSuccess());
        System.out.println("Pesan          : " + response.getMessage());
        if (response.getMember() != null) {
            printMember(response.getMember());
        }
    }

    private static void printMemberPage(String title, MemberPage page) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(title);
        System.out.println("============================================================");
        System.out.println("Total data     : " + page.getTotalItems());
        System.out.println("Halaman        : " + page.getPage() + "/" + page.getTotalPages());
        for (MemberSummary member : page.getMembers()) {
            printMember(member);
        }
    }

    private static void printMember(MemberSummary member) {
        System.out.println("ID User        : " + member.getIdUser());
        System.out.println("Username       : " + member.getUsername());
        System.out.println("Nama           : " + member.getNama());
        System.out.println("Email          : " + member.getEmail());
        System.out.println("Role           : " + member.getRole());
        System.out.println("Status Akun    : " + member.getStatusAkun());
        System.out.println("Created At     : " + member.getCreatedAt());
    }
}
