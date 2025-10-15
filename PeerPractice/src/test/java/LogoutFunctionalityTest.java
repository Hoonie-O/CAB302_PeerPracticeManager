import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.UserManager;
import com.cab302.peerpractice.Model.Managers.UserSession;
import com.cab302.peerpractice.Model.Utils.BcryptHasher;
import com.cab302.peerpractice.Model.Utils.PasswordHasher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for logout functionality
 * Tests cover UserSession logout behavior and UserManager authentication state
 */
public class LogoutFunctionalityTest {

    private UserSession userSession;
    private User testUser;
    private IUserDAO userDao;
    private UserManager userManager;

    @BeforeEach
    public void setUp() throws SQLException {
        userSession = new UserSession();
        userDao = new UserDAO(); // real DAO
        PasswordHasher passwordHasher = new BcryptHasher();
        userManager = new UserManager(userDao, passwordHasher);

        // Use unique identifiers to avoid clashes between test runs
        String ts = String.valueOf(System.currentTimeMillis());
        testUser = new User("John", "Doe", "johndoe_" + ts,
                "john" + ts + "@test.com", "hashedpass", "QUT");

        userDao.addUser(testUser);
    }

    @AfterEach
    public void tearDown() {
        try {
            if (testUser != null) {
                userDao.deleteUser(testUser.getUserId());
            }
        } catch (Exception ignored) {
        }
    }

    // UserSession logout tests
    @Test
    public void testUserSessionLogout_WhenUserLoggedIn_ShouldSetCurrentUserToNull() {
        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());

        userSession.logout();

        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    @Test
    public void testUserSessionLogout_WhenNoUserLoggedIn_ShouldRemainLoggedOut() {
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());

        userSession.logout();

        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    @Test
    public void testUserSessionLogout_MultipleLogoutCalls_ShouldBeSafeToCall() {
        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());

        userSession.logout();
        userSession.logout();
        userSession.logout();

        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    @Test
    public void testUserSessionState_AfterLogoutAndRelogin_ShouldWorkCorrectly() {
        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());

        userSession.logout();
        assertFalse(userSession.isLoggedIn());

        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());
        assertEquals(testUser, userSession.getCurrentUser());
    }

    // Integration tests with UserManager
    @Test
    public void testLogoutIntegration_AfterSuccessfulAuthentication_ShouldClearSession() throws Exception {
        String ts = String.valueOf(System.currentTimeMillis());
        String username = "john_doe_" + ts;
        String email = "john" + ts + "@test.com";

        userManager.signUp("John", "Doe", username, email, "Password1!", "QUT");
        assertTrue(userManager.authenticate(username, "Password1!"));

        User actualUser = userDao.getUserByUsername(username).orElseThrow();
        userSession.setCurrentUser(actualUser);
        assertTrue(userSession.isLoggedIn());

        userSession.logout();

        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
        assertTrue(userDao.existsByUsername(username));

        // cleanup
        userDao.deleteUser(actualUser.getUserId());
    }

    @Test
    public void testLogoutIntegration_ShouldNotAffectUserData_InDAO() {
        userSession.setCurrentUser(testUser);
        String originalEmail = testUser.getEmail();
        String originalUsername = testUser.getUsername();

        userSession.logout();

        assertTrue(userDao.existsByEmail(originalEmail));
        assertTrue(userDao.existsByUsername(originalUsername));
        var userFromDao = userDao.getUserByUsername(originalUsername);
        assertTrue(userFromDao.isPresent());
        assertEquals(originalEmail, userFromDao.get().getEmail());
    }

    // Edge cases
    @Test
    public void testLogoutSecurity_ShouldClearSensitiveSessionData() {
        User sensitiveUser = new User("Admin", "User", "admin123", "admin@test.com", "supersecret", "QUT");
        userSession.setCurrentUser(sensitiveUser);

        userSession.logout();

        assertNull(userSession.getCurrentUser());
        assertFalse(userSession.isLoggedIn());
    }

    @Test
    public void testLogoutConsistency_IsLoggedInShouldMatchCurrentUserState() {
        assertEquals(userSession.getCurrentUser() != null, userSession.isLoggedIn());

        userSession.setCurrentUser(testUser);
        assertEquals(userSession.getCurrentUser() != null, userSession.isLoggedIn());

        userSession.logout();
        assertEquals(userSession.getCurrentUser() != null, userSession.isLoggedIn());
    }

    @Test
    public void testLogoutThreadSafety_MultipleLogoutCalls_ShouldBeSafe() {
        userSession.setCurrentUser(testUser);

        Runnable logoutTask = () -> {
            for (int i = 0; i < 10; i++) {
                userSession.logout();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignored) {
                }
            }
        };

        Thread t1 = new Thread(logoutTask);
        Thread t2 = new Thread(logoutTask);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            fail("Thread interrupted");
        }

        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }
}