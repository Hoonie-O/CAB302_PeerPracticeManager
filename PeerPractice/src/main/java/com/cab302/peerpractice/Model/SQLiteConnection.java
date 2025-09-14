package com.cab302.peerpractice.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteConnection{
    /**
     * Connects to the SQLite database
     */
    private static Connection instance = null;

    private SQLiteConnection() throws SQLException {
        String url = "jdbc:sqlite:PeerPracticeManager.db";
        try {
            instance = DriverManager.getConnection(url);
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
