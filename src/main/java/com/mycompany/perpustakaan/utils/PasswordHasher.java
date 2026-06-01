package com.mycompany.perpustakaan.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHasher {

    private PasswordHasher() {
    }

    public static boolean matches(String plainPassword, String storedPassword) {
        if (plainPassword == null || storedPassword == null) {
            return false;
        }

        String normalizedHash = normalizeBcryptHash(storedPassword);
        if (!normalizedHash.startsWith("$2")) {
            return plainPassword.equals(storedPassword);
        }

        return BCrypt.checkpw(plainPassword, normalizedHash);
    }

    private static String normalizeBcryptHash(String storedPassword) {
        if (storedPassword.startsWith("$2y$") || storedPassword.startsWith("$2x$") || storedPassword.startsWith("$2b$")) {
            return "$2a$" + storedPassword.substring(4);
        }
        return storedPassword;
    }
}