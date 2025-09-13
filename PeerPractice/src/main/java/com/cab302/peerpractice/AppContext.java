package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.*;

public class AppContext {
    private final UserSession userSession = new UserSession();
    private final IUserDAO userDao = new MockDAO();
    private final PasswordHasher passwordHasher = new BcryptHasher();
    private final UserManager userManager = new UserManager(userDao,passwordHasher);
    private final MailService mailService = new MailService();
    private final EventManager eventManager = new EventManager();

    public UserSession getUserSession(){return userSession;}
    public IUserDAO getUserDao(){return userDao;}
    public PasswordHasher getPasswordHasher(){return passwordHasher;}
    public UserManager getUserManager(){return userManager;}
    public MailService getMailService(){return mailService;}
    public EventManager getEventManager(){return eventManager;}

}
