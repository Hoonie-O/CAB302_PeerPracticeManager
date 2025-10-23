package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.DAOs.*;
import com.cab302.peerpractice.Model.Factories.DAOFactory;
import com.cab302.peerpractice.Model.Factories.ManagerFactory;
import com.cab302.peerpractice.Model.Initializers.TestDataInitializer;
import com.cab302.peerpractice.Model.Managers.*;
import com.cab302.peerpractice.Model.Utils.PasswordHasher;

import java.sql.SQLException;

/**
 * Application context providing access to services, managers, and application state.
 *
 * REFACTORED to follow the Factory Method pattern and Single Responsibility Principle.
 * This class now delegates creation to specialized factories and focuses on
 * coordinating application-level concerns.
 *
 * Architecture:
 * - DAOFactory: Creates and manages all DAO instances
 * - ManagerFactory: Creates and manages all Manager/Service instances
 * - TestDataInitializer: Handles test data setup (separated concern)
 *
 * @see DAOFactory
 * @see ManagerFactory
 * @see TestDataInitializer
 */
public class AppContext {

    // Core application components
    private final UserSession userSession;
    private final DAOFactory daoFactory;
    private final ManagerFactory managerFactory;

    // UI state (will be refactored to separate UIStateManager in future iteration)
    private boolean menuOpen = false;
    private boolean profileOpen = false;

    /**
     * Creates the application context and initializes all services.
     *
     * @throws SQLException if database initialization fails
     */
    public AppContext() throws SQLException {
        try {
            // Initialize user session
            this.userSession = new UserSession();

            // Initialize factories
            this.daoFactory = DAOFactory.getInstance();
            this.managerFactory = new ManagerFactory(daoFactory);

            // Initialize test data (only in development/test environments)
            if (TestDataInitializer.isEnabled()) {
                TestDataInitializer testDataInitializer = new TestDataInitializer(
                    daoFactory.getUserDAO(),
                    daoFactory.getGroupDAO(),
                    managerFactory.getUserManager()
                );
                testDataInitializer.initializeTestData();
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize AppContext", e);
        }
    }

    // ========================================
    // PUBLIC API - Manager Access (PREFERRED)
    // ========================================

    /**
     * Gets the user session manager.
     *
     * @return the UserSession
     */
    public UserSession getUserSession() {
        return userSession;
    }

    /**
     * Gets the user authentication service (PREFERRED).
     *
     * Use this for authentication operations: signup, login, password changes.
     *
     * @return the UserAuthenticationService
     */
    public com.cab302.peerpractice.Model.Services.UserAuthenticationService getUserAuthenticationService() {
        return managerFactory.getUserAuthenticationService();
    }

    /**
     * Gets the user profile service (PREFERRED).
     *
     * Use this for profile updates: name, phone, address, bio, etc.
     *
     * @return the UserProfileService
     */
    public com.cab302.peerpractice.Model.Services.UserProfileService getUserProfileService() {
        return managerFactory.getUserProfileService();
    }

    /**
     * Gets the user manager for user-related operations.
     *
     * @return the UserManager
     * @deprecated Use {@link #getUserAuthenticationService()} for authentication
     *             and {@link #getUserProfileService()} for profile updates.
     */
    @Deprecated
    public UserManager getUserManager() {
        return managerFactory.getUserManager();
    }

    /**
     * Gets the group operation service (PREFERRED).
     *
     * Use this for group operations: create, delete, member management, join requests.
     *
     * @return the GroupOperationService
     */
    public com.cab302.peerpractice.Model.Services.GroupOperationService getGroupOperationService() {
        return managerFactory.getGroupOperationService();
    }

    /**
     * Gets the group role service (PREFERRED).
     *
     * Use this for role management: promote, demote, role checking.
     *
     * @return the GroupRoleService
     */
    public com.cab302.peerpractice.Model.Services.GroupRoleService getGroupRoleService() {
        return managerFactory.getGroupRoleService();
    }

    /**
     * Gets the group manager for group-related operations.
     *
     * @return the GroupManager
     * @deprecated Use {@link #getGroupOperationService()} for group operations
     *             and {@link #getGroupRoleService()} for role management.
     */
    @Deprecated
    public GroupManager getGroupManager() {
        return managerFactory.getGroupManager();
    }

    /**
     * Gets the notes manager for note-related operations.
     *
     * @return the NotesManager
     */
    public NotesManager getNotesManager() {
        return managerFactory.getNotesManager();
    }

    /**
     * Gets the session manager for session-related operations.
     *
     * @return the SessionManager
     */
    public SessionManager getSessionManager() {
        return managerFactory.getSessionManager();
    }

    /**
     * Gets the session task manager.
     *
     * @return the SessionTaskManager
     */
    public SessionTaskManager getSessionTaskManager() {
        return managerFactory.getSessionTaskManager();
    }

    /**
     * Gets the session calendar manager.
     *
     * @return the SessionCalendarManager
     */
    public SessionCalendarManager getSessionCalendarManager() {
        return managerFactory.getSessionCalendarManager();
    }

    /**
     * Gets the availability manager.
     *
     * @return the AvailabilityManager
     */
    public AvailabilityManager getAvailabilityManager() {
        return managerFactory.getAvailabilityManager();
    }

    /**
     * Gets the group message manager.
     *
     * @return the GroupMessageManager
     */
    public GroupMessageManager getGroupMessageManager() {
        return managerFactory.getGroupMessageManager();
    }

    /**
     * Gets the group file manager.
     *
     * @return the GroupFileManager
     */
    public GroupFileManager getGroupFileManager() {
        return managerFactory.getGroupFileManager();
    }

    /**
     * Gets the password hasher utility.
     *
     * @return the PasswordHasher
     */
    public PasswordHasher getPasswordHasher() {
        return managerFactory.getPasswordHasher();
    }

    /**
     * Gets the notifier service.
     *
     * @return the Notifier
     */
    public Notifier getNotifier() {
        return managerFactory.getNotifier();
    }

    /**
     * Gets the mail service.
     *
     * @return the MailService
     */
    public MailService getMailService() {
        return managerFactory.getMailService();
    }

    // ========================================
    // DEPRECATED - Direct DAO Access
    // ========================================
    // These methods provide backward compatibility but should NOT be used in new code.
    // Controllers should use Managers instead of DAOs for proper layering.
    // Will be removed in a future version.

    /**
     * @deprecated Use {@link #getUserManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public IUserDAO getUserDAO() {
        return daoFactory.getUserDAO();
    }

    /**
     * @deprecated Use {@link #getGroupManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public IFriendDAO getFriendDAO() {
        return daoFactory.getFriendDAO();
    }

    /**
     * @deprecated Use {@link #getGroupManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public IGroupDAO getGroupDAO() {
        return daoFactory.getGroupDAO();
    }

    /**
     * @deprecated Use {@link #getNotesManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public INotesDAO getNotesDAO() {
        return daoFactory.getNotesDAO();
    }

    /**
     * @deprecated Use {@link #getSessionCalendarManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public ISessionCalendarDAO getSessionCalendarDAO() {
        return daoFactory.getSessionCalendarDAO();
    }

    /**
     * @deprecated Use {@link #getSessionTaskManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public ISessionTaskDAO getSessionTaskDAO() {
        return daoFactory.getSessionTaskDAO();
    }

    /**
     * @deprecated Use {@link #getAvailabilityManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public IAvailabilityDAO getAvailabilityDAO() {
        return daoFactory.getAvailabilityDAO();
    }

    /**
     * @deprecated Use {@link #getGroupMessageManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public IGroupMessageDAO getGroupMessageDAO() {
        return daoFactory.getGroupMessageDAO();
    }

    /**
     * @deprecated Use appropriate manager instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public IFriendMessageDAO getFriendMessageDAO() {
        return daoFactory.getFriendMessageDAO();
    }

    /**
     * @deprecated Use {@link #getGroupFileManager()} instead.
     * Direct DAO access violates the service layer pattern.
     */
    @Deprecated
    public IGroupFileDAO getGroupFileDAO() {
        return daoFactory.getGroupFileDAO();
    }

    // ========================================
    // UI State Management
    // TODO: Extract to separate UIStateManager in future iteration
    // ========================================

    public boolean isMenuOpen() {
        return menuOpen;
    }

    public void setMenuOpen(boolean value) {
        this.menuOpen = value;
    }

    public boolean isProfileOpen() {
        return profileOpen;
    }

    public void setProfileOpen(boolean value) {
        this.profileOpen = value;
    }
}
