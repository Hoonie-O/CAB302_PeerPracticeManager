import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.*;
import com.cab302.peerpractice.View;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MainMenuController logout functionality
 * Tests core logic of logout operations without JavaFX UI dependencies
 */
public class DefaultControllerLogoutTest {

    private AppContext appContext;
    private MockNavigation mockNavigation;
    private UserSession userSession;
    private User testUser;

    @BeforeEach
    public void setUp() {
        // Set up test dependencies
        appContext = new AppContext();
        userSession = appContext.getUserSession();
        mockNavigation = new MockNavigation();
        
        // Create a test user and log them in
        testUser = new User("Test", "User", "testuser", "test@example.com", "password", "QUT");
        userSession.setCurrentUser(testUser);
    }

    @Test
    public void testLogoutClearsUserSession() {
        // User is logged in
        assertTrue(userSession.isLoggedIn());
        assertEquals(testUser, userSession.getCurrentUser());
        
        // Simulate logout operation (the core logic from performLogout)
        userSession.logout();
        
        // User session should be cleared
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    @Test
    public void testLogoutNavigatesToLogin() {
        // User is logged in
        assertTrue(userSession.isLoggedIn());
        
        // Simulate complete logout flow
        userSession.logout();
        mockNavigation.Display(View.Login);
        
        // Should navigate to login and clear session
        assertFalse(userSession.isLoggedIn());
        assertEquals(View.Login, mockNavigation.getLastDisplayedView());
    }

    @Test
    public void testUserProfileInitialization_WithLoggedInUser() {
        // User is logged in
        assertTrue(userSession.isLoggedIn());
        assertEquals(testUser, userSession.getCurrentUser());
        
        // Simulate profile initialization logic
        var currentUser = userSession.getCurrentUser();
        String expectedDisplayName = currentUser.getFirstName() + " " + currentUser.getLastName();
        String expectedUsername = "@" + currentUser.getUsername();
        
        // Profile data should be correctly formatted
        assertEquals("Test User", expectedDisplayName);
        assertEquals("@testuser", expectedUsername);
    }

    @Test
    public void testUserProfileInitialization_WithoutLoggedInUser() {
        // No user logged in
        userSession.logout();
        assertFalse(userSession.isLoggedIn());
        
        // Simulate profile initialization with no user
        var currentUser = userSession.getCurrentUser();
        String displayName = currentUser != null ? 
            currentUser.getFirstName() + " " + currentUser.getLastName() : "Not logged in";
        String username = currentUser != null ? 
            "@" + currentUser.getUsername() : "@unknown";
        
        //Should handle null user gracefully
        assertEquals("Not logged in", displayName);
        assertEquals("@unknown", username);
    }

    @Test
    public void testLogoutSecurityCleanup() {
        //User with sensitive data is logged in
        User sensitiveUser = new User("Admin", "Super", "admin", "admin@secure.com", "topsecret", "SECURE_ORG");
        userSession.setCurrentUser(sensitiveUser);
        assertTrue(userSession.isLoggedIn());
        
        // Logout
        userSession.logout();
        
        // No sensitive data should remain accessible through session
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
        
        // Additional security check - ensure we can't access user data
        var sessionUser = userSession.getCurrentUser();
        assertNull(sessionUser);
    }

    @Test
    public void testLogoutFromDifferentStates() {
        // Test 1: logout when already logged out (should be safe)
        userSession.logout();
        assertFalse(userSession.isLoggedIn());
        
        // Should be safe to call again
        userSession.logout();
        assertFalse(userSession.isLoggedIn());
        
        // Test 2: login and logout cycle
        userSession.setCurrentUser(testUser);
        assertTrue(userSession.isLoggedIn());
        
        userSession.logout();
        assertFalse(userSession.isLoggedIn());
        
        // Test 3: Multiple users (simulate switching users then logging out)
        User anotherUser = new User("Jane", "Doe", "janedoe", "jane@test.com", "pass", "UQ");
        userSession.setCurrentUser(anotherUser);
        assertTrue(userSession.isLoggedIn());
        assertEquals(anotherUser, userSession.getCurrentUser());
        
        userSession.logout();
        assertFalse(userSession.isLoggedIn());
        assertNull(userSession.getCurrentUser());
    }

    /**
     * Mock Navigation class for testing navigation behavior without the JavaFX stuff
     */
    private static class MockNavigation {
        private View lastDisplayedView;
        
        public void Display(View view) {
            this.lastDisplayedView = view;
        }
        
        public View getLastDisplayedView() {
            return lastDisplayedView;
        }
    }
}