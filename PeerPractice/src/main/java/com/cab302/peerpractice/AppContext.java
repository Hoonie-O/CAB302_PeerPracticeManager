package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class AppContext {
    private final UserSession userSession = new UserSession();
    private final IUserDAO userDao = new UserDAO();
    private final IGroupDAO groupDao;
    private final Notifier notifier = new Notifier(userDao);
    private final PasswordHasher passwordHasher = new BcryptHasher();
    private final UserManager userManager = new UserManager(userDao,passwordHasher);
    private final GroupManager groupManager;
    private final MailService mailService = new MailService();
    private final SessionManager sessionManager;
    private final SessionTaskStorage sessionTaskStorage;
    private final SessionTaskManager sessionTaskManager;
    private final SessionCalendarManager sessionCalendarManager;
    private final AvailabilityManager availabilityManager;
    private final INotesDAO notesDAO = new MockNotesDao();
    private final NotesManager notesManager;
    private boolean menuOpen = false;
    private boolean profileOpen = false;

    public AppContext() throws SQLException {
        try {
            this.groupDao = new GroupDBDAO(userDao);
            this.groupManager = new GroupManager(groupDao, notifier, userDao);
            this.notesManager = new NotesManager(notesDAO, groupDao);

            var sessionStorage = new SessionCalendarDBStorage(userDao);
            this.sessionCalendarManager = new SessionCalendarManager(sessionStorage);
            this.sessionManager = new SessionManager(this.sessionCalendarManager);
            this.sessionTaskStorage = new SessionTaskDBStorage(userDao);
            this.sessionTaskManager = new SessionTaskManager(sessionTaskStorage, this.sessionManager);
            
            this.sessionCalendarManager.setSessionTaskManager(this.sessionTaskManager);

            var availabilityStorage = new AvailabilityDBStorage(userDao);
            this.availabilityManager = new AvailabilityManager(availabilityStorage);

            User testUser = userDao.findUser("username", "Testuser17");

            if (testUser != null) {
                Group testGroup = new Group("Example Group", "This is a seeded test group", false,
                        testUser.getUsername(), LocalDateTime.now());
//                testGroup.setMembers(new ArrayList<>());
//                testGroup.addMember(testUser);
                this.groupDao.addGroup(testGroup);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed test data", e);
        }
    }

    public UserSession getUserSession(){return userSession;}
    public IUserDAO getUserDao(){return userDao;}
    public PasswordHasher getPasswordHasher(){return passwordHasher;}
    public UserManager getUserManager(){return userManager;}
    public MailService getMailService(){return mailService;}
    public GroupManager getGroupManager(){return  groupManager;}
    public IGroupDAO getGroupDao() {return groupDao;}
    public SessionManager getSessionManager(){return sessionManager;}
    public SessionTaskManager getSessionTaskManager(){return sessionTaskManager;}
    public SessionCalendarManager getSessionCalendarManager(){return sessionCalendarManager;}
    public AvailabilityManager getAvailabilityManager(){return availabilityManager;}
    public NotesManager getNotesManager(){return notesManager;}
    public INotesDAO getNotesDAO(){return notesDAO;}
    public boolean isMenuOpen() { return menuOpen; }
    public void setMenuOpen(boolean value) { this.menuOpen = value; }
    public boolean isProfileOpen() { return profileOpen; }
    public void setProfileOpen(boolean value) { this.profileOpen = value; }

}
