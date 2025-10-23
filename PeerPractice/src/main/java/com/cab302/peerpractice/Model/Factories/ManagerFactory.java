package com.cab302.peerpractice.Model.Factories;

import com.cab302.peerpractice.Model.DAOs.*;
import com.cab302.peerpractice.Model.Managers.*;
import com.cab302.peerpractice.Model.Services.UserAuthenticationService;
import com.cab302.peerpractice.Model.Services.UserProfileService;
import com.cab302.peerpractice.Model.Services.GroupOperationService;
import com.cab302.peerpractice.Model.Services.GroupRoleService;
import com.cab302.peerpractice.Model.Utils.BcryptHasher;
import com.cab302.peerpractice.Model.Utils.PasswordHasher;

/**
 * Factory for creating Manager/Service instances.
 * Implements the Factory Method pattern to centralize manager creation
 * and manage complex dependencies between managers.
 *
 * Benefits:
 * - Encapsulates complex manager initialization logic
 * - Single point of configuration for manager wiring
 * - Resolves circular dependencies
 * - Enables testing through mock factories
 */
public class ManagerFactory {

    private final DAOFactory daoFactory;
    private final PasswordHasher passwordHasher;
    private final Notifier notifier;
    private final MailService mailService;

    // Cached manager instances (DEPRECATED - being replaced by focused services)
    private UserManager userManager;
    private GroupManager groupManager;
    private NotesManager notesManager;
    private SessionCalendarManager sessionCalendarManager;
    private SessionManager sessionManager;
    private SessionTaskManager sessionTaskManager;
    private AvailabilityManager availabilityManager;
    private GroupMessageManager groupMessageManager;
    private GroupFileManager groupFileManager;

    // Cached service instances (NEW - focused single-responsibility services)
    private UserAuthenticationService userAuthenticationService;
    private UserProfileService userProfileService;
    private GroupOperationService groupOperationService;
    private GroupRoleService groupRoleService;

    /**
     * Creates a ManagerFactory with the given DAO factory.
     *
     * @param daoFactory the DAO factory for data access
     */
    public ManagerFactory(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
        this.passwordHasher = new BcryptHasher();
        this.notifier = new Notifier(daoFactory.getUserDAO(), daoFactory.getFriendDAO());
        this.mailService = new MailService();
    }

    /**
     * Gets or creates the UserAuthenticationService instance (PREFERRED).
     *
     * This is the new focused service for authentication operations.
     * Replaces authentication-related methods from UserManager.
     *
     * @return the UserAuthenticationService
     */
    public UserAuthenticationService getUserAuthenticationService() {
        if (userAuthenticationService == null) {
            userAuthenticationService = new UserAuthenticationService(
                    daoFactory.getUserDAO(),
                    passwordHasher
            );
        }
        return userAuthenticationService;
    }

    /**
     * Gets or creates the UserProfileService instance (PREFERRED).
     *
     * This is the new focused service for profile management operations.
     * Replaces profile-related methods from UserManager.
     *
     * @return the UserProfileService
     */
    public UserProfileService getUserProfileService() {
        if (userProfileService == null) {
            userProfileService = new UserProfileService(daoFactory.getUserDAO());
        }
        return userProfileService;
    }

    /**
     * Gets or creates the UserManager instance.
     *
     * @return the UserManager
     * @deprecated Use {@link #getUserAuthenticationService()} for authentication
     *             and {@link #getUserProfileService()} for profile updates.
     *             This monolithic manager will be removed in a future version.
     */
    @Deprecated
    public UserManager getUserManager() {
        if (userManager == null) {
            userManager = new UserManager(daoFactory.getUserDAO(), passwordHasher);
        }
        return userManager;
    }

    /**
     * Gets or creates the GroupOperationService instance (PREFERRED).
     *
     * This is the new focused service for group operations.
     * Replaces CRUD and member management methods from GroupManager.
     *
     * @return the GroupOperationService
     */
    public GroupOperationService getGroupOperationService() {
        if (groupOperationService == null) {
            groupOperationService = new GroupOperationService(
                    daoFactory.getGroupDAO(),
                    daoFactory.getUserDAO(),
                    notifier
            );
        }
        return groupOperationService;
    }

    /**
     * Gets or creates the GroupRoleService instance (PREFERRED).
     *
     * This is the new focused service for role management.
     * Replaces role-related methods from GroupManager.
     *
     * @return the GroupRoleService
     */
    public GroupRoleService getGroupRoleService() {
        if (groupRoleService == null) {
            groupRoleService = new GroupRoleService(daoFactory.getGroupDAO());
        }
        return groupRoleService;
    }

    /**
     * Gets or creates the GroupManager instance.
     *
     * @return the GroupManager
     * @deprecated Use {@link #getGroupOperationService()} for group operations
     *             and {@link #getGroupRoleService()} for role management.
     *             This monolithic manager will be removed in a future version.
     */
    @Deprecated
    public GroupManager getGroupManager() {
        if (groupManager == null) {
            groupManager = new GroupManager(
                daoFactory.getGroupDAO(),
                notifier,
                daoFactory.getUserDAO()
            );
        }
        return groupManager;
    }

    /**
     * Gets or creates the NotesManager instance.
     *
     * @return the NotesManager
     */
    public NotesManager getNotesManager() {
        if (notesManager == null) {
            notesManager = new NotesManager(
                daoFactory.getNotesDAO(),
                daoFactory.getGroupDAO()
            );
        }
        return notesManager;
    }

    /**
     * Gets or creates the SessionCalendarManager instance.
     *
     * @return the SessionCalendarManager
     */
    public SessionCalendarManager getSessionCalendarManager() {
        if (sessionCalendarManager == null) {
            sessionCalendarManager = new SessionCalendarManager(
                daoFactory.getSessionCalendarDAO()
            );

            // Resolve circular dependency after both managers are created
            if (sessionTaskManager != null) {
                sessionCalendarManager.setSessionTaskManager(sessionTaskManager);
            }
        }
        return sessionCalendarManager;
    }

    /**
     * Gets or creates the SessionManager instance.
     *
     * @return the SessionManager
     */
    public SessionManager getSessionManager() {
        if (sessionManager == null) {
            sessionManager = new SessionManager(getSessionCalendarManager());
        }
        return sessionManager;
    }

    /**
     * Gets or creates the SessionTaskManager instance.
     *
     * @return the SessionTaskManager
     */
    public SessionTaskManager getSessionTaskManager() {
        if (sessionTaskManager == null) {
            sessionTaskManager = new SessionTaskManager(
                daoFactory.getSessionTaskDAO(),
                getSessionManager()
            );

            // Resolve circular dependency
            if (sessionCalendarManager != null) {
                sessionCalendarManager.setSessionTaskManager(sessionTaskManager);
            }
        }
        return sessionTaskManager;
    }

    /**
     * Gets or creates the AvailabilityManager instance.
     *
     * @return the AvailabilityManager
     */
    public AvailabilityManager getAvailabilityManager() {
        if (availabilityManager == null) {
            availabilityManager = new AvailabilityManager(
                daoFactory.getAvailabilityDAO()
            );
        }
        return availabilityManager;
    }

    /**
     * Gets or creates the GroupMessageManager instance.
     *
     * @return the GroupMessageManager
     */
    public GroupMessageManager getGroupMessageManager() {
        if (groupMessageManager == null) {
            groupMessageManager = new GroupMessageManager(
                daoFactory.getGroupMessageDAO()
            );
        }
        return groupMessageManager;
    }

    /**
     * Gets or creates the GroupFileManager instance.
     *
     * @return the GroupFileManager
     */
    public GroupFileManager getGroupFileManager() {
        if (groupFileManager == null) {
            groupFileManager = new GroupFileManager(
                daoFactory.getGroupFileDAO()
            );
        }
        return groupFileManager;
    }

    /**
     * Gets the PasswordHasher instance.
     *
     * @return the PasswordHasher
     */
    public PasswordHasher getPasswordHasher() {
        return passwordHasher;
    }

    /**
     * Gets the Notifier instance.
     *
     * @return the Notifier
     */
    public Notifier getNotifier() {
        return notifier;
    }

    /**
     * Gets the MailService instance.
     *
     * @return the MailService
     */
    public MailService getMailService() {
        return mailService;
    }

    /**
     * Resets all cached manager instances (for testing).
     */
    public void reset() {
        // Reset legacy managers
        userManager = null;
        groupManager = null;
        notesManager = null;
        sessionCalendarManager = null;
        sessionManager = null;
        sessionTaskManager = null;
        availabilityManager = null;
        groupMessageManager = null;
        groupFileManager = null;

        // Reset new focused services
        userAuthenticationService = null;
        userProfileService = null;
        groupOperationService = null;
        groupRoleService = null;
    }
}
