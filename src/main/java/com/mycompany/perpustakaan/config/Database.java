package com.mycompany.perpustakaan.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/perpustakaan?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";
    private static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        loadDriver();

        String url = getSetting("db.url", "DB_URL", DEFAULT_URL);
        String user = getSetting("db.user", "DB_USER", DEFAULT_USER);
        String password = getSetting("db.password", "DB_PASSWORD", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException exception) {
            return false;
        }
    }

    private static void loadDriver() {
        String driver = getSetting("db.driver", "DB_DRIVER", DEFAULT_DRIVER);
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("MySQL JDBC driver not found: " + driver, exception);
        }
    }

    private static String getSetting(String systemProperty, String envKey, String defaultValue) {
        String value = System.getProperty(systemProperty);
        if (value == null || value.isBlank()) {
            value = System.getenv(envKey);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}