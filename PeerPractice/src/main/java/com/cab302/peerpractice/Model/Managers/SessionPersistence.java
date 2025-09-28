package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Entities.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Handles persistent user sessions for "Remember Me" functionality
 */
public class SessionPersistence {
    private static final String SESSION_FILE = "user_session.properties";
    private static final String USER_DIR = System.getProperty("user.home") + "/.peerpractice";

    public static void saveSession(User user, boolean remember) {
        if (!remember) {
            clearSession();
            return;
        }

        try {
            // create app directory if needed
            Path userDirPath = Paths.get(USER_DIR);
            Files.createDirectories(userDirPath);

            Properties props = new Properties();
            props.setProperty("userId", user.getUserId());
            props.setProperty("username", user.getUsername());
            props.setProperty("expiry", LocalDateTime.now().plusDays(30).toString());

            Path sessionFile = userDirPath.resolve(SESSION_FILE);
            try (FileOutputStream out = new FileOutputStream(sessionFile.toFile())) {
                props.store(out, "PeerPractice persistent session");
            }
        } catch (IOException e) {
            System.err.println("Failed to save session: " + e.getMessage());
        }
    }

    public static User loadSavedSession(IUserDAO userDao) {
        try {
            Path sessionFile = Paths.get(USER_DIR, SESSION_FILE);
            if (!Files.exists(sessionFile)) {
                return null;
            }

            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream(sessionFile.toFile())) {
                props.load(in);
            }

            String expiryStr = props.getProperty("expiry");
            if (expiryStr != null) {
                LocalDateTime expiry = LocalDateTime.parse(expiryStr);
                if (LocalDateTime.now().isAfter(expiry)) {
                    clearSession();
                    return null;
                }
            }

            String userId = props.getProperty("userId");
            if (userId != null) {
                return userDao.findUserById(userId);
            }

        } catch (Exception e) {
            System.err.println("Failed to load session: " + e.getMessage());
            clearSession();
        }

        return null;
    }

    public static void clearSession() {
        try {
            Path sessionFile = Paths.get(USER_DIR, SESSION_FILE);
            Files.deleteIfExists(sessionFile);
        } catch (IOException e) {
            System.err.println("Failed to clear session: " + e.getMessage());
        }
    }
}