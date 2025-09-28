import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Notification;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestNotification extends Notification {
    private final String message;

    public TestNotification(User from, String to, String message) {
        super(from, to);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}

class UserDAOTest {

    private Connection memConn;
    private UserDAO userDao;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() throws SQLException {
        // In-memory DB
        memConn = DriverManager.getConnection("jdbc:sqlite::memory:");
        SQLiteConnection.setInstance(memConn);

        userDao = new UserDAO();

        // Seed users
        alice = new User("1", "Alice", "Wonder", "alice123",
                "alice@mail.com", "Password1!", "QUT");
        bob = new User("2", "Bob", "Builder", "bob123",
                "bob@mail.com", "Password2!", "QUT");

        userDao.addUser(alice);
        userDao.addUser(bob);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (memConn != null && !memConn.isClosed()) {
            memConn.close();
        }
        SQLiteConnection.reset();
    }

    // -------------------- CREATE --------------------
    @Test
    void createUserWithId_success() throws Exception {
        User charlie = new User("3", "Charlie", "Chaplin", "charlie123",
                "charlie@mail.com", "Password3!", "UQ");
        assertTrue(userDao.addUser(charlie));
        assertNotNull(userDao.findUserById("3"));
    }

    @Test
    void createUserWithDuplicateUsername_throwsException() {
        assertThrows(DuplicateUsernameException.class, () -> {
            userDao.createUserWithId("99", "alice123", "x", "A", "W", "alice2@mail.com", "QUT");
        });
    }

    @Test
    void createUserWithDuplicateEmail_throwsException() {
        assertThrows(DuplicateEmailException.class, () -> {
            userDao.createUserWithId("100", "aliceDifferent", "x", "A", "W", "alice@mail.com", "QUT");
        });
    }

    // -------------------- READ --------------------
    @Test
    void findUserById_returnsCorrectUser() throws SQLException {
        User found = userDao.findUserById("1");
        assertNotNull(found);
        assertEquals("alice123", found.getUsername());
    }

    @Test
    void getUserByEmail_returnsOptional() {
        Optional<User> found = userDao.getUserByEmail("alice@mail.com");
        assertTrue(found.isPresent());
        assertEquals("alice123", found.get().getUsername());
    }

    @Test
    void getUserByUsername_returnsOptional() {
        Optional<User> found = userDao.getUserByUsername("bob123");
        assertTrue(found.isPresent());
        assertEquals("bob@mail.com", found.get().getEmail());
    }

    @Test
    void searchByInstitution_returnsMatchingUsers() {
        List<User> results = userDao.searchByInstitution("QUT");
        assertEquals(2, results.size());
    }

    @Test
    void existsByEmail_and_existsByUsername() {
        assertTrue(userDao.existsByEmail("alice@mail.com"));
        assertTrue(userDao.existsByUsername("alice123"));
        assertFalse(userDao.existsByEmail("nobody@mail.com"));
    }

    // -------------------- UPDATE --------------------
    @Test
    void updateValue_changesField() throws SQLException {
        assertTrue(userDao.updateValue("alice123", "first_name", "Alicia"));
        User updated = userDao.findUserById("1");
        assertEquals("Alicia", updated.getFirstName());
    }

    @Test
    void updateUser_updatesMultipleFields() {
        alice.setFirstName("Alicia");
        alice.setLastName("Wonders");
        alice.setEmail("newalice@mail.com");
        alice.setInstitution("UQ");

        assertTrue(userDao.updateUser(alice));

        User updated = userDao.searchByUsername("alice123");
        assertEquals("Alicia", updated.getFirstName());
        assertEquals("UQ", updated.getInstitution());
    }

    @Test
    void storePassword_updatesPassword() {
        assertTrue(userDao.storePassword(bob, "newHash"));
    }

    // -------------------- DELETE --------------------
    @Test
    void deleteUser_removesUser() throws SQLException {
        assertTrue(userDao.deleteUser(alice));
        assertNull(userDao.findUserById("1"));
    }

    @Test
    void deleteUserById_removesUser() throws SQLException {
        assertTrue(userDao.deleteUser("2"));
        assertNull(userDao.findUserById("2"));
    }

    // -------------------- NOTIFICATIONS --------------------
    @Test
    void addNotification_insertsRow() {
        assertTrue(userDao.addNotification(alice.getUserId(), bob.getUserId(), "Hello!"));
    }

    @Test
    void addNotificationWithObject_insertsRow() {
        Notification n = new TestNotification(alice, bob.getUserId(), "Reminder: study session");
        assertTrue(userDao.addNotification(alice.getUserId(), n));
    }
}
