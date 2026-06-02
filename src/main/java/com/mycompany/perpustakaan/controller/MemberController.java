package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.api.MemberRequest;
import com.mycompany.perpustakaan.dao.PeminjamanDao;
import com.mycompany.perpustakaan.dao.UserDao;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.PasswordHasher;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;
import java.util.List;

public class MemberController {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserDao userDao;
    private final PeminjamanDao peminjamanDao;

    public MemberController() {
        this.userDao = new UserDao();
        this.peminjamanDao = new PeminjamanDao();
    }

    public User addMember(MemberRequest request) throws SQLException {
        requireStaffOrAdmin();
        MemberRequest safeRequest = validateMemberRequest(request, true);
        String hashedPassword = PasswordHasher.hash(safeRequest.getPassword());
        return userDao.insertMember(safeRequest, hashedPassword);
    }

    public User updateMember(int idUser, MemberRequest request) throws SQLException {
        requireStaffOrAdmin();
        validateIdUser(idUser);
        MemberRequest safeRequest = validateMemberRequest(request, false);
        String hashedPassword = safeRequest.getPassword() == null ? null : PasswordHasher.hash(safeRequest.getPassword());

        User member = userDao.updateMember(idUser, safeRequest, hashedPassword);
        if (member == null) {
            throw new IllegalStateException("Anggota tidak ditemukan.");
        }
        return member;
    }

    public User suspendMember(int idUser) throws SQLException {
        requireStaffOrAdmin();
        validateIdUser(idUser);
        User member = userDao.updateMemberStatus(idUser, "suspend");
        if (member == null) {
            throw new IllegalStateException("Anggota tidak ditemukan.");
        }
        return member;
    }

    public User activateMember(int idUser) throws SQLException {
        requireStaffOrAdmin();
        validateIdUser(idUser);
        User member = userDao.updateMemberStatus(idUser, "aktif");
        if (member == null) {
            throw new IllegalStateException("Anggota tidak ditemukan.");
        }
        return member;
    }

    public boolean deleteMember(int idUser) throws SQLException {
        requireStaffOrAdmin();
        validateIdUser(idUser);

        User member = userDao.findById(idUser);
        if (member == null || !member.isAnggota()) {
            throw new IllegalStateException("Anggota tidak ditemukan.");
        }

        if (peminjamanDao.countLoansByUser(idUser) > 0) {
            throw new IllegalStateException("Anggota tidak bisa dihapus karena sudah memiliki data peminjaman. Gunakan suspend.");
        }

        return userDao.deleteMemberById(idUser);
    }

    public List<User> searchMembers(String keyword, String statusAkun, int page, int pageSize) throws SQLException {
        requireStaffOrAdmin();
        int safePage = normalizePage(page);
        int safePageSize = normalizePageSize(pageSize);
        int offset = (safePage - 1) * safePageSize;
        return userDao.searchMembers(normalizeText(keyword), normalizeStatus(statusAkun), safePageSize, offset);
    }

    public int countMembers(String keyword, String statusAkun) throws SQLException {
        requireStaffOrAdmin();
        return userDao.countMembers(normalizeText(keyword), normalizeStatus(statusAkun));
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

    public String normalizeStatus(String statusAkun) {
        if (statusAkun == null || statusAkun.isBlank() || "semua".equalsIgnoreCase(statusAkun) || "all".equalsIgnoreCase(statusAkun)) {
            return null;
        }

        String normalizedStatus = statusAkun.trim().toLowerCase();
        if (!"aktif".equals(normalizedStatus) && !"suspend".equals(normalizedStatus)) {
            throw new IllegalArgumentException("Status anggota tidak valid.");
        }
        return normalizedStatus;
    }

    private User requireStaffOrAdmin() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User harus login sebelum mengakses member management.");
        }
        if (!currentUser.isStaff() && !currentUser.isAdmin()) {
            throw new IllegalStateException("Hanya staff atau admin yang boleh mengakses member management.");
        }
        return currentUser;
    }

    private MemberRequest validateMemberRequest(MemberRequest request, boolean passwordRequired) {
        if (request == null) {
            throw new IllegalArgumentException("Data anggota wajib diisi.");
        }

        String username = requireText(request.getUsername(), "Username wajib diisi.");
        String nama = requireText(request.getNama(), "Nama anggota wajib diisi.");
        String email = normalizeEmail(request.getEmail());
        String password = normalizePassword(request.getPassword(), passwordRequired);

        return new MemberRequest(username, nama, email, password);
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalizedEmail = email.trim();
        if (!normalizedEmail.contains("@")) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
        return normalizedEmail;
    }

    private String normalizePassword(String password, boolean required) {
        if (password == null || password.isBlank()) {
            if (required) {
                throw new IllegalArgumentException("Password wajib diisi.");
            }
            return null;
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password minimal 6 karakter.");
        }
        return password;
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private void validateIdUser(int idUser) {
        if (idUser < 1) {
            throw new IllegalArgumentException("ID anggota tidak valid.");
        }
    }
}
