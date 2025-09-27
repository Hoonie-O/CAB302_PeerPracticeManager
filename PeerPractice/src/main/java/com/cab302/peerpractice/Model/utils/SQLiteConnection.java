package com.cab302.peerpractice.Model.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection{
    /**
     * Connects to the SQLite database
     */
    private static Connection instance = null;

    private SQLiteConnection() throws SQLException {
        try {
            Path cwd = Paths.get(System.getProperty("user.dir"));
            Path moduleDir = cwd.resolve("PeerPractice");
            Path dbPath;
            if (Files.isDirectory(moduleDir)) {
                dbPath = moduleDir.resolve("PeerPracticeManager.db");
            } else {
                dbPath = cwd.resolve("PeerPracticeManager.db");
            }
            if (dbPath.getParent() != null) {
                try { Files.createDirectories(dbPath.getParent()); } catch (Exception ignored) {}
            }
            String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
            instance = DriverManager.getConnection(url);
            try (var st = instance.createStatement()) { st.execute("PRAGMA foreign_keys = ON"); }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e);
        }
    }

    public static Connection getInstance() throws SQLException {
        if (instance == null) {
            new SQLiteConnection();
        }
        return instance;
    }
}
