package com.mycompany.perpustakaan.controller;

import com.mycompany.perpustakaan.dao.BukuDao;
import com.mycompany.perpustakaan.dao.UserDao;
import com.mycompany.perpustakaan.model.Buku;
import com.mycompany.perpustakaan.model.User;
import com.mycompany.perpustakaan.utils.SessionManager;
import java.sql.SQLException;
import java.util.List;

public class DashboardController {

    private final BukuDao bukuDao;
    private final UserDao userDao;

    public DashboardController() {
        this.bukuDao = new BukuDao();
        this.userDao = new UserDao();
    }

    public User getProfile() throws SQLException {
        if (!SessionManager.isLoggedIn()) {
            return null;
        }
<<<<<<< HEAD

        User currentUser = SessionManager.getCurrentUser();
        return userDao.findById(currentUser.getIdUser());
=======
        User sessionUser = SessionManager.getCurrentUser();
        // refresh from DB to get latest fields
        return userDao.findById(sessionUser.getIdUser());
>>>>>>> 971d3bf3dfadeb3f2eb35438307861006fcebde6
    }

    public int getTotalBooks() throws SQLException {
        return bukuDao.countAll();
    }

    public List<Buku> getLatestBooks(int limit) throws SQLException {
        return bukuDao.findLatest(limit);
    }

    public List<Buku> searchBooks(String keyword, int limit, int offset) throws SQLException {
        return bukuDao.search(keyword, limit, offset);
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> 971d3bf3dfadeb3f2eb35438307861006fcebde6
