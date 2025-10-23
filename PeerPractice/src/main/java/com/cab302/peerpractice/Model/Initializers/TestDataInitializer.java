package com.cab302.peerpractice.Model.Initializers;

import com.cab302.peerpractice.Model.DAOs.IGroupDAO;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.UserManager;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Responsible for initializing test/demo data in the application.
 * Follows the Single Responsibility Principle by separating data initialization
 * from application context setup.
 *
 * Benefits:
 * - Clear separation of concerns
 * - Easy to disable in production
 * - Testable initialization logic
 * - Configurable test data
 */
public class TestDataInitializer {

    private final IUserDAO userDAO;
    private final IGroupDAO groupDAO;
    private final UserManager userManager;

    /**
     * Creates a test data initializer.
     *
     * @param userDAO the user DAO
     * @param groupDAO the group DAO
     * @param userManager the user manager for signup
     */
    public TestDataInitializer(IUserDAO userDAO, IGroupDAO groupDAO, UserManager userManager) {
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO cannot be null");
        this.groupDAO = Objects.requireNonNull(groupDAO, "GroupDAO cannot be null");
        this.userManager = Objects.requireNonNull(userManager, "UserManager cannot be null");
    }

    /**
     * Initializes all test data.
     * Creates test users and groups if they don't exist.
     */
    public void initializeTestData() {
        try {
            User john = ensureTestUser(
                "John",
                "Doe",
                "Testuser17",
                "testjohn@mail.com",
                "Testuser17$",
                "QUT"
            );

            User jane = ensureTestUser(
                "Jane",
                "Doe",
                "Testuser18",
                "testjane@mail.com",
                "Testuser18$",
                "QUT"
            );

            ensureExampleGroup(john);
        } catch (Exception e) {
            System.err.println("Error initializing test data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensures a test user exists, creating them if necessary.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param username the username
     * @param email the email
     * @param password the plain password
     * @param institution the institution
     * @return the User (existing or newly created)
     */
    private User ensureTestUser(String firstName, String lastName, String username,
                                 String email, String password, String institution) {
        try {
            User existing = userDAO.findUser("username", username);
            if (existing != null) {
                return existing;
            }

            // Create new user
            userManager.signUp(firstName, lastName, username, email, password, institution);
            return userDAO.findUser("username", username);
        } catch (Exception e) {
            System.err.println("Error creating test user " + username + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ensures the example group exists, creating it if necessary.
     *
     * @param owner the owner of the group
     */
    private void ensureExampleGroup(User owner) {
        try {
            if (owner == null) {
                System.err.println("Cannot create example group: owner is null");
                return;
            }

            if (!groupDAO.existsByName("Example Group")) {
                Group exampleGroup = new Group(
                    "Example Group",
                    "This is a test group for demonstration purposes",
                    false,
                    owner,
                    LocalDateTime.now()
                );
                groupDAO.addGroup(exampleGroup);
            }

            // Ensure owner is in the group
            groupDAO.addToGroup(1, owner);
        } catch (Exception e) {
            System.err.println("Error creating example group: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if test data initialization is enabled.
     * Can be controlled via system property or environment variable.
     *
     * @return true if test data should be initialized
     */
    public static boolean isEnabled() {
        String env = System.getProperty("app.environment", "development");
        return "development".equalsIgnoreCase(env) || "test".equalsIgnoreCase(env);
    }
}
