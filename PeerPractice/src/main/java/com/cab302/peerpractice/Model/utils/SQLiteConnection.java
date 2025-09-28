package com.cab302.peerpractice.Model.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton manager for SQLite connections.
 * Defaults to a file-backed DB, but allows test code to inject an in-memory connection.
 */
public class SQLiteConnection {
    private static Connection instance = null;

    private SQLiteConnection() { /* prevent instantiation */ }

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            instance = createFileConnection();
        }
        return instance;
    }

    public static void setInstance(Connection conn) throws SQLException {
        if (conn != null) {
            instance = conn;
            try (var st = instance.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
        }
    }

    public static void reset() throws SQLException {
        if (instance != null && !instance.isClosed()) {
            instance.close();
        }
        instance = null;
    }

    private static Connection createFileConnection() throws SQLException {
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
        Connection conn = DriverManager.getConnection(url);
        try (var st = conn.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }
}
