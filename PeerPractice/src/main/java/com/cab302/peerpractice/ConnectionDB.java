package com.cab302.peerpractice;

import com.sun.rowset.CachedRowSetImpl;
import java.sql.*;

/**
 * Database operation utilities
 */
public class ConnectionDB
{
    // Declare the MySQL JDBC driver to interact with database
    private static final String jdbcDriver = "com.mysql.cj.jdbc.Driver";
    // Declare the URL used to connect to the MySQL database
    private static final String urlString = "jdbc:mysql://localhost:3306/PeerManager";
    // Declare Connection variable
    private static Connection conn = null;

    // Connect to MySQL database
    public static void connectToDB() throws ClassNotFoundException, SQLException {
        // Set up driver
        try {
            Class.forName(jdbcDriver);

        } catch (ClassNotFoundException e) {
            System.out.println("JDBC driver not found" + e);
            throw e;
        }

        // Establish connection
        try {
            conn = DriverManager.getConnection(urlString);
        } catch (SQLException e) {
            System.out.println("Failed to connect" + e);
            throw e;
        }
    }

    // Disconnect from MySQL database
    public static void disconnectFromDB() throws SQLException {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e){
            System.out.println("Failed to disconnect" + e);
            throw e;
        }
    }

    // Execute query on MySQL database
    public static ResultSet executeQuery(String queryStatement) throws ClassNotFoundException, SQLException {
        // Declare statement and result set
        Statement stmt = null;
        ResultSet result = null;
        CachedRowSetImpl cachedRowSet = null;

        try {
            connectToDB();

            // Perform select operation
            stmt = conn.createStatement();
            result = stmt.executeQuery(queryStatement);

            cachedRowSet = new CachedRowSetImpl();
            cachedRowSet.populate(result);
        } catch (SQLException e) {
            System.out.println("Query failed to execute" + e);
            throw e;
        } finally {
            // Close result set and statement
            if (result != null) {
                result.close();
            }
            if (stmt != null) {
                stmt.close();
            }

            disconnectFromDB();
        }

        return cachedRowSet;
    }

    public static void executeUpdate (String updateStatement) throws ClassNotFoundException, SQLException {
        // Declare statement
        Statement stmt = null;

        try {
            connectToDB();

            // Perform select operation
            stmt = conn.createStatement();
            stmt.executeUpdate(updateStatement);
        } catch (SQLException e) {
            System.out.println("Update failed to execute" + e);
            throw e;
        } finally {
            // Close statement
            if (stmt != null) {
                stmt.close();
            }

            disconnectFromDB();
        }
    }
}
