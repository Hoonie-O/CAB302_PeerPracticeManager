package com.cab302.peerpractice.Model.Factories;

import com.cab302.peerpractice.Model.DAOs.*;

import java.sql.SQLException;

/**
 * Factory for creating DAO instances.
 * Implements the Factory Method pattern to centralize DAO creation
 * and enable dependency injection without tight coupling.
 *
 * Benefits:
 * - Single point of configuration for DAO creation
 * - Easy to swap implementations (e.g., SQLite -> PostgreSQL)
 * - Testability through mock factory
 */
public class DAOFactory {

    private static DAOFactory instance;
    private final IUserDAO userDAO;
    private final IGroupDAO groupDAO;
    private final IFriendDAO friendDAO;
    private final INotesDAO notesDAO;
    private final ISessionCalendarDAO sessionCalendarDAO;
    private final ISessionTaskDAO sessionTaskDAO;
    private final IAvailabilityDAO availabilityDAO;
    private final IGroupMessageDAO groupMessageDAO;
    private final IFriendMessageDAO friendMessageDAO;
    private final IGroupFileDAO groupFileDAO;

    /**
     * Private constructor initializes all DAOs.
     *
     * @throws SQLException if database initialization fails
     */
    private DAOFactory() throws SQLException {
        // Initialize all DAOs
        this.userDAO = new UserDAO();
        this.groupDAO = new GroupDAO(userDAO);
        this.friendDAO = new FriendDAO(userDAO);
        this.notesDAO = new NotesDAO();
        this.sessionCalendarDAO = new SessionCalendarDAO(userDAO);
        this.sessionTaskDAO = new SessionTaskDAO(userDAO);
        this.availabilityDAO = new AvailabilityDAO(userDAO);
        this.groupMessageDAO = new GroupMessageDAO();
        this.friendMessageDAO = new FriendMessageDAO();
        this.groupFileDAO = new GroupFileDAO();
    }

    /**
     * Gets the singleton instance of the DAO factory.
     *
     * @return the DAO factory instance
     * @throws SQLException if initialization fails
     */
    public static synchronized DAOFactory getInstance() throws SQLException {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    /**
     * Creates a new factory instance (for testing).
     *
     * @return a new DAO factory
     * @throws SQLException if initialization fails
     */
    public static DAOFactory createNew() throws SQLException {
        return new DAOFactory();
    }

    // DAO Getters
    public IUserDAO getUserDAO() {
        return userDAO;
    }

    public IGroupDAO getGroupDAO() {
        return groupDAO;
    }

    public IFriendDAO getFriendDAO() {
        return friendDAO;
    }

    public INotesDAO getNotesDAO() {
        return notesDAO;
    }

    public ISessionCalendarDAO getSessionCalendarDAO() {
        return sessionCalendarDAO;
    }

    public ISessionTaskDAO getSessionTaskDAO() {
        return sessionTaskDAO;
    }

    public IAvailabilityDAO getAvailabilityDAO() {
        return availabilityDAO;
    }

    public IGroupMessageDAO getGroupMessageDAO() {
        return groupMessageDAO;
    }

    public IFriendMessageDAO getFriendMessageDAO() {
        return friendMessageDAO;
    }

    public IGroupFileDAO getGroupFileDAO() {
        return groupFileDAO;
    }

    /**
     * Resets the singleton instance (for testing).
     */
    public static synchronized void resetInstance() {
        instance = null;
    }
}
