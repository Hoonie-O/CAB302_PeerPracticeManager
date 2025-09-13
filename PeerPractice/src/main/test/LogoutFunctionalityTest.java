import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for logout functionality
 * Tests cover UserSession logout behavior and UserManager authentication state
 */
public class LogoutFunctionalityTest {

    private UserSession userSession;
    private User testUser;
    private IUserDAO mockDao;
    private PasswordHasher passwordHasher;
    private UserManager userManager;

    @BeforeEach
    public void setUp() {
        userSession = new UserSession();
        testUser = new User("John", "Doe", "johndoe", "john@test.com", "hashedpass", "QUT");
        mockDao = new MockDAO();
        passwordHasher = new BcryptHasher();
        userManager = new UserManager(mockDao, passwordHasher);
    }

    // UserSession logout tests
    @Test
    public void testUserSessionLogout_WhenUserLoggedIn_ShouldSetCurrentUserToNull() {
        //User is logged in
        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());
        assertNotNull(userSession.getCurrentUser());
        
        // Logout
        userSession.logout();
        
        // User should be logged out
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    @Test
    public void testUserSessionLogout_WhenNoUserLoggedIn_ShouldRemainLoggedOut() {
        // No user logged in
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
        
        // Logout (should be safe to call)
        userSession.logout();
        
        // Should still be logged out
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    @Test
    public void testUserSessionLogout_MultipleLogoutCalls_ShouldBeSafeToCall() {
        // User logged in
        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());
        
        // Multiple logout calls
        userSession.logout();
        userSession.logout();
        userSession.logout();
        
        //Should be safely logged out
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    @Test
    public void testUserSessionState_AfterLogoutAndRelogin_ShouldWorkCorrectly() {
        // Initial login
        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());
        
        // Logout and login again
        userSession.logout();
        assertFalse(userSession.isLoggedIn());
        userSession.setCurrentUser(testUser);
        
        //Should be logged in again
        assertTrue(userSession.isLoggedIn());
        assertEquals(testUser, userSession.getCurrentUser());
    }

    // Integration tests with UserManager
    @Test
    public void testLogoutIntegration_AfterSuccessfulAuthentication_ShouldClearSession() {
        //Sign up user properly (this handles password hashing)
        userManager.signUp("John", "Doe", "johndoe", "john@test.com", "password", "QUT");
        assertTrue(userManager.authenticate("johndoe", "password"));
        // Get the actual user from DAO (with proper hashed password)
        User actualUser = mockDao.getUserByUsername("johndoe").orElseThrow();
        userSession.setCurrentUser(actualUser);
        assertTrue(userSession.isLoggedIn());
        
        // Logout
        userSession.logout();
        
        // Session should be cleared but user still exists in DAO
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
        // User should still exist in DAO for future logins
        assertTrue(mockDao.existsByUsername("johndoe"));
    }

    @Test
    public void testLogoutIntegration_ShouldNotAffectUserData_InDAO() {
        // User data in DAO
        mockDao.addUser(testUser);
        userSession.setCurrentUser(testUser);
        String originalEmail = testUser.getEmail();
        String originalUsername = testUser.getUsername();
        
        // Logout
        userSession.logout();
        
        // User data in DAO is unchanged
        assertTrue(mockDao.existsByEmail(originalEmail));
        assertTrue(mockDao.existsByUsername(originalUsername));
        var userFromDao = mockDao.getUserByUsername(originalUsername);
        assertTrue(userFromDao.isPresent());
        assertEquals(originalEmail, userFromDao.get().getEmail());
    }

    // Edge cases
    @Test
    public void testLogoutSecurity_ShouldClearSensitiveSessionData() {
        // Arrange: User with sensitive data logged in
        User sensitiveUser = new User("Admin", "User", "admin", "admin@test.com", "supersecret", "QUT");
        userSession.setCurrentUser(sensitiveUser);
        
        // Logout
        userSession.logout();
        
        // No reference to sensitive user data should remain in session
        assertNull(userSession.getCurrentUser());
        assertFalse(userSession.isLoggedIn());
    }

    @Test
    public void testLogoutConsistency_IsLoggedInShouldMatchCurrentUserState() {
        // Test that isLoggedIn() always matches the state of getCurrentUser()
        
        // Initial state
        assertEquals(userSession.getCurrentUser() != null, userSession.isLoggedIn());
        
        // After login
        userSession.setCurrentUser(testUser);
        assertEquals(userSession.getCurrentUser() != null, userSession.isLoggedIn());
        
        // After logout
        userSession.logout();
        assertEquals(userSession.getCurrentUser() != null, userSession.isLoggedIn());
    }

    // Concurrency safety test (basic)
    @Test
    public void testLogoutThreadSafety_MultipleLogoutCalls_ShouldBeSafe() {
        // User logged in
        userSession.setCurrentUser(testUser);
        
        // Simulate multiple rapid logout calls
        Runnable logoutTask = () -> {
            for (int i = 0; i < 10; i++) {
                userSession.logout();
                try { Thread.sleep(1); } catch (InterruptedException e) { }
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