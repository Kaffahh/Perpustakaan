package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.api.BookRequest;
import com.mycompany.perpustakaan.dao.BukuDao;
import com.mycompany.perpustakaan.dao.PeminjamanDao;
import com.mycompany.perpustakaan.model.Buku;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;
import java.time.Year;

public class StaffBookController {

    private final BukuDao bukuDao;
    private final PeminjamanDao peminjamanDao;

    public StaffBookController() {
        this.bukuDao = new BukuDao();
        this.peminjamanDao = new PeminjamanDao();
    }

    public Buku addBook(BookRequest request) throws SQLException {
        User currentUser = requireStaffOrAdmin();
        BookRequest safeRequest = validateBookRequest(request);
        return bukuDao.insert(safeRequest, currentUser.getIdUser());
    }

    public Buku updateBook(int idBuku, BookRequest request) throws SQLException {
        requireStaffOrAdmin();
        if (idBuku < 1) {
            throw new IllegalArgumentException("ID buku tidak valid.");
        }

        BookRequest safeRequest = validateBookRequest(request);
        Buku updatedBook = bukuDao.update(idBuku, safeRequest);
        if (updatedBook == null) {
            throw new IllegalStateException("Buku tidak ditemukan.");
        }
        return updatedBook;
    }

    public Buku updateStock(int idBuku, int stokTersedia, int stokTotal) throws SQLException {
        requireStaffOrAdmin();
        if (idBuku < 1) {
            throw new IllegalArgumentException("ID buku tidak valid.");
        }
        validateStock(stokTersedia, stokTotal);

        Buku updatedBook = bukuDao.updateStock(idBuku, stokTersedia, stokTotal);
        if (updatedBook == null) {
            throw new IllegalStateException("Buku tidak ditemukan.");
        }
        return updatedBook;
    }

    public boolean deleteBook(int idBuku) throws SQLException {
        requireStaffOrAdmin();
        if (idBuku < 1) {
            throw new IllegalArgumentException("ID buku tidak valid.");
        }
        if (bukuDao.findById(idBuku) == null) {
            throw new IllegalStateException("Buku tidak ditemukan.");
        }
        if (peminjamanDao.countLoansByBook(idBuku) > 0) {
            throw new IllegalStateException("Buku tidak bisa dihapus karena sudah memiliki data peminjaman.");
        }
        return bukuDao.deleteById(idBuku);
    }

    public Buku getBookById(int idBuku) throws SQLException {
        requireStaffOrAdmin();
        if (idBuku < 1) {
            throw new IllegalArgumentException("ID buku tidak valid.");
        }

        Buku book = bukuDao.findById(idBuku);
        if (book == null) {
            throw new IllegalStateException("Buku tidak ditemukan.");
        }
        return book;
    }

    private User requireStaffOrAdmin() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User harus login sebelum mengakses manajemen buku.");
        }
        if (!currentUser.isStaff() && !currentUser.isAdmin()) {
            throw new IllegalStateException("Hanya staff atau admin yang boleh mengakses manajemen buku.");
        }
        return currentUser;
    }

    private BookRequest validateBookRequest(BookRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Data buku wajib diisi.");
        }

        String kodeBuku = requireText(request.getKodeBuku(), "Kode buku wajib diisi.");
        String judul = requireText(request.getJudul(), "Judul buku wajib diisi.");
        String penulis = requireText(request.getPenulis(), "Penulis buku wajib diisi.");
        String penerbit = normalizeOptionalText(request.getPenerbit());
        String kategori = normalizeOptionalText(request.getKategori());
        Integer tahunTerbit = validateYear(request.getTahunTerbit());
        validateStock(request.getStokTersedia(), request.getStokTotal());

        return new BookRequest(kodeBuku, judul, penulis, penerbit, kategori, tahunTerbit, request.getStokTersedia(), request.getStokTotal());
    }

    private Integer validateYear(Integer tahunTerbit) {
        if (tahunTerbit == null) {
            return null;
        }
        int currentYear = Year.now().getValue();
        if (tahunTerbit < 1000 || tahunTerbit > currentYear) {
            throw new IllegalArgumentException("Tahun terbit tidak valid.");
        }
        return tahunTerbit;
    }

    private void validateStock(int stokTersedia, int stokTotal) {
        if (stokTersedia < 0 || stokTotal < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        }
        if (stokTersedia > stokTotal) {
            throw new IllegalArgumentException("Stok tersedia tidak boleh lebih besar dari stok total.");
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
