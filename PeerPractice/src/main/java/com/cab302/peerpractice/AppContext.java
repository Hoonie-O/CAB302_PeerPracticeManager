package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.daos.*;
import com.cab302.peerpractice.Model.entities.Group;
import com.cab302.peerpractice.Model.entities.User;
import com.cab302.peerpractice.Model.managers.*;
import com.cab302.peerpractice.Model.utils.BcryptHasher;
import com.cab302.peerpractice.Model.utils.PasswordHasher;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AppContext {
    private final UserSession userSession = new UserSession();

    // --- DAO layer ---
    private final IUserDAO userDAO;
    private final IFriendDAO friendDAO;
    private final IGroupDAO groupDAO;
    private final INotesDAO notesDAO;
    private final ISessionCalendarDAO sessionCalendarDAO;
    private final ISessionTaskDAO sessionTaskDAO;
    private final IAvailabilityDAO availabilityDAO;

    // --- Managers & Services ---
    private final Notifier notifier;
    private final PasswordHasher passwordHasher;
    private final UserManager userManager;
    private final GroupManager groupManager;
    private final MailService mailService;
    private final SessionManager sessionManager;
    private final SessionTaskManager sessionTaskManager;
    private final SessionCalendarManager sessionCalendarManager;
    private final AvailabilityManager availabilityManager;
    private final NotesManager notesManager;

    private boolean menuOpen = false;
    private boolean profileOpen = false;

    public AppContext() throws SQLException {
        try {
            // DAO instantiation
            this.userDAO = new UserDAO();
            this.friendDAO = new FriendDAO(userDAO);
            this.groupDAO = new GroupDAO(userDAO);
            this.notesDAO = new NotesDAO();
            this.sessionCalendarDAO = new SessionCalendarDAO(userDAO);
            this.sessionTaskDAO = new SessionTaskDAO(userDAO);
            this.availabilityDAO = new AvailabilityDAO(userDAO);

            // Services & utilities
            this.notifier = new Notifier(userDAO);
            this.passwordHasher = new BcryptHasher();
            this.mailService = new MailService();

            // Managers
            this.userManager = new UserManager(userDAO, passwordHasher);
            this.groupManager = new GroupManager(groupDAO, notifier, userDAO);
            this.notesManager = new NotesManager(notesDAO, groupDAO);

            this.sessionCalendarManager = new SessionCalendarManager(sessionCalendarDAO);
            this.sessionManager = new SessionManager(this.sessionCalendarManager);
            this.sessionTaskManager = new SessionTaskManager(sessionTaskDAO, this.sessionManager);

            this.sessionCalendarManager.setSessionTaskManager(this.sessionTaskManager);

            this.availabilityManager = new AvailabilityManager(availabilityDAO);

            // --- Ensure John Doe exists ---
            User john = userDAO.findUser("username", "Testuser17");
            if (john == null) {
                try {
                    this.userManager.signUp(
                            "John",                 // first name
                            "Doe",                  // last name
                            "Testuser17",           // username
                            "testjohn@mail.com",    // email
                            "Testuser17$",          // password (plain, manager will hash/validate)
                            "QUT"                   // institution
                    );
                    john = userDAO.findUser("username", "Testuser17");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // --- Ensure Jane Doe exists ---
            User jane = userDAO.findUser("username", "Testuser18");
            if (jane == null) {
                try {
                    this.userManager.signUp(
                            "Jane",
                            "Doe",
                            "Testuser18",
                            "testjane@mail.com",
                            "Testuser18$",
                            "QUT"
                    );
                    jane = userDAO.findUser("username", "Testuser18");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // --- Always ensure group exists for John Doe ---
            if (!groupDAO.existsByName("Example Group")) {
                Group testGroup = new Group(
                        "Example Group",
                        "This is a test group",
                        false,
                        john.getUsername(),
                        LocalDateTime.now()
                );
                this.groupDAO.addGroup(testGroup);
            }
            this.groupDAO.addToGroup(1, john);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialise AppContext", e);
        }
    }

    // --- Getters ---
    public UserSession getUserSession() { return userSession; }
    public IUserDAO getUserDAO() { return userDAO; }
    public IFriendDAO getFriendDAO() { return friendDAO; }
    public IGroupDAO getGroupDAO() { return groupDAO; }
    public INotesDAO getNotesDAO() { return notesDAO; }
    public ISessionCalendarDAO getSessionCalendarDAO() { return sessionCalendarDAO; }
    public ISessionTaskDAO getSessionTaskDAO() { return sessionTaskDAO; }
    public IAvailabilityDAO getAvailabilityDAO() { return availabilityDAO; }

    public PasswordHasher getPasswordHasher() { return passwordHasher; }
    public Notifier getNotifier() { return notifier; }
    public MailService getMailService() { return mailService; }

    public UserManager getUserManager() { return userManager; }
    public GroupManager getGroupManager() { return groupManager; }
    public NotesManager getNotesManager() { return notesManager; }
    public SessionManager getSessionManager() { return sessionManager; }
    public SessionTaskManager getSessionTaskManager() { return sessionTaskManager; }
    public SessionCalendarManager getSessionCalendarManager() { return sessionCalendarManager; }
    public AvailabilityManager getAvailabilityManager() { return availabilityManager; }

    public boolean isMenuOpen() { return menuOpen; }
    public void setMenuOpen(boolean value) { this.menuOpen = value; }
    public boolean isProfileOpen() { return profileOpen; }
    public void setProfileOpen(boolean value) { this.profileOpen = value; }

}
