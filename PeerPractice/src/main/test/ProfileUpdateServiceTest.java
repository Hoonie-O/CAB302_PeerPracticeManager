import com.cab302.peerpractice.Model.daos.IUserDAO;
import com.cab302.peerpractice.Model.daos.UserDAO;
import com.cab302.peerpractice.Model.entities.User;
import com.cab302.peerpractice.Model.managers.ProfileUpdateService;
import com.cab302.peerpractice.Model.managers.UserManager;
import com.cab302.peerpractice.Model.utils.BcryptHasher;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ProfileUpdateServiceTest {

private ProfileUpdateService service;
private UserManager userManager;
private IUserDAO userDao;
private User testUser;

@BeforeEach
void setUp() throws SQLException {
    userDao = new UserDAO();

    // Clean up in case test users exist from previous runs
    try { userDao.deleteUser("johndoe"); } catch (Exception ignored) {}
    try { userDao.deleteUser("janesmith"); } catch (Exception ignored) {}

    userManager = new UserManager(userDao, new BcryptHasher());
    service = new ProfileUpdateService(userManager);

    testUser = new User("John", "Doe", "johndoe", "john@example.com", "hashedpass", "QUT");
    testUser.setPhone("123456789");
    testUser.setAddress("123 Main St");
    testUser.setDateOfBirth("1990-01-01");

    userDao.addUser(testUser);
}

@AfterEach
void tearDown() {
    try { userDao.deleteUser(testUser.getUserId()); } catch (Exception ignored) {}
}

// === Core Tests ===

@Test
void updateProfile_noChanges_returnsFalse() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getUsername(),
            testUser.getInstitution(),
            testUser.getPhone(),
            testUser.getAddress(),
            LocalDate.parse("1990-01-01")
    );
    assertFalse(service.updateProfile(testUser, request));
}

@Test
void updateProfile_firstNameChanged_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            "Jane", testUser.getLastName(), testUser.getUsername(),
            testUser.getInstitution(), testUser.getPhone(),
            testUser.getAddress(), LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("Jane", testUser.getFirstName());
}

@Test
void updateProfile_lastNameChanged_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), "Smith", testUser.getUsername(),
            testUser.getInstitution(), testUser.getPhone(),
            testUser.getAddress(), LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("Smith", testUser.getLastName());
}

@Test
void updateProfile_usernameChanged_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            "newusername", testUser.getInstitution(),
            testUser.getPhone(), testUser.getAddress(),
            LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("newusername", testUser.getUsername());
}

@Test
void updateProfile_institutionChanged_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            testUser.getUsername(), "UQ",
            testUser.getPhone(), testUser.getAddress(),
            LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("UQ", testUser.getInstitution());
}

@Test
void updateProfile_phoneChanged_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            testUser.getUsername(), testUser.getInstitution(),
            "987654321", testUser.getAddress(),
            LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("987654321", testUser.getPhone());
}

@Test
void updateProfile_addressChanged_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            testUser.getUsername(), testUser.getInstitution(),
            testUser.getPhone(), "456 Oak Ave",
            LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("456 Oak Ave", testUser.getAddress());
}

@Test
void updateProfile_dateOfBirthChanged_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            testUser.getUsername(), testUser.getInstitution(),
            testUser.getPhone(), testUser.getAddress(),
            LocalDate.parse("1985-06-15")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("1985-06-15", testUser.getDateOfBirth());
}

// === Edge Cases ===

@Test
void updateProfile_nullDateOfBirth_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            testUser.getUsername(), testUser.getInstitution(),
            testUser.getPhone(), testUser.getAddress(),
            null
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("", testUser.getDateOfBirth());
}

@Test
void updateProfile_invalidFirstName_throwsException() {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            "", testUser.getLastName(),
            testUser.getUsername(), testUser.getInstitution(),
            testUser.getPhone(), testUser.getAddress(),
            LocalDate.parse("1990-01-01")
    );
    assertThrows(IllegalArgumentException.class, () -> service.updateProfile(testUser, request));
}

@Test
void updateProfile_invalidLastName_throwsException() {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), null,
            testUser.getUsername(), testUser.getInstitution(),
            testUser.getPhone(), testUser.getAddress(),
            LocalDate.parse("1990-01-01")
    );
    assertThrows(IllegalArgumentException.class, () -> service.updateProfile(testUser, request));
}

@Test
void updateProfile_emptyInstitution_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            testUser.getUsername(), "",
            testUser.getPhone(), testUser.getAddress(),
            LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("", testUser.getInstitution());
}

@Test
void updateProfile_unicodeCharacters_returnsTrue() throws SQLException {
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            "José", "García",
            testUser.getUsername(), "Universidad Nacional",
            testUser.getPhone(), "Calle de la Paz 123",
            LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals("José", testUser.getFirstName());
    assertEquals("García", testUser.getLastName());
}

@Test
void updateProfile_longFields_returnsTrue() throws SQLException {
    String longName = "A".repeat(50);
    String longInstitution = "University ".repeat(10);
    String longPhone = "1".repeat(20);
    String longAddress = "Very long address ".repeat(20);

    var request = new ProfileUpdateService.ProfileUpdateRequest(
            longName, longName, testUser.getUsername(),
            longInstitution, longPhone, longAddress,
            LocalDate.parse("1990-01-01")
    );
    assertTrue(service.updateProfile(testUser, request));
}

@Test
void updateProfile_futureDate_returnsTrue() throws SQLException {
    LocalDate futureDate = LocalDate.now().plusYears(10);
    var request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(), testUser.getLastName(),
            testUser.getUsername(), testUser.getInstitution(),
            testUser.getPhone(), testUser.getAddress(),
            futureDate
    );
    assertTrue(service.updateProfile(testUser, request));
    assertEquals(futureDate.toString(), testUser.getDateOfBirth());
}

@Test
    void updateProfile_pastDate_returnsTrue() throws SQLException {
        LocalDate pastDate = LocalDate.of(1950, 1, 1);
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getUsername(),
            testUser.getInstitution(),
            testUser.getPhone(),
            testUser.getAddress(),
            pastDate
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
        assertEquals(pastDate.toString(), testUser.getDateOfBirth());
    }

    @Test
    void updateProfile_whitespaceFieldsHandled() throws SQLException {
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            "  Jane  ",
            "  Smith  ",
            testUser.getUsername(),
            "  University  ",
            "  987654321  ",
            "  456 Oak Ave  ",
            LocalDate.parse("1990-01-01")
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
    }

    @Test
    void updateProfile_partialChanges_onlyChangedFieldsUpdated() throws SQLException {
        String originalFirstName = testUser.getFirstName();
        String originalLastName = testUser.getLastName();
        String originalUsername = testUser.getUsername();
        String originalInstitution = testUser.getInstitution();
        String originalAddress = testUser.getAddress();
        String originalDateOfBirth = testUser.getDateOfBirth();

        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            originalFirstName,
            originalLastName,
            originalUsername,
            originalInstitution,
            "999888777",
            originalAddress,
            LocalDate.parse(originalDateOfBirth)
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
        assertEquals("999888777", testUser.getPhone());
        assertEquals(originalFirstName, testUser.getFirstName());
        assertEquals(originalLastName, testUser.getLastName());
    }

    @Test
    void updateProfile_sameValues_returnsFalse() throws SQLException {
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getUsername(),
            testUser.getInstitution(),
            testUser.getPhone(),
            testUser.getAddress(),
            LocalDate.parse(testUser.getDateOfBirth())
        );

        boolean result = service.updateProfile(testUser, request);
        assertFalse(result);
    }

    @Test
    void updateProfile_normalizeFields_handlesEdgeCases() throws SQLException {
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            "JANE",
            "SMITH",
            "janesmith123",
            "UNIVERSITY OF QUEENSLAND",
            "(07) 1234-5678",
            "123 MAIN STREET, BRISBANE QLD 4000",
            LocalDate.parse("1990-01-01")
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
    }

    @Test
    void updateProfile_extremeDateValues_handledCorrectly() throws SQLException {
        LocalDate minDate = LocalDate.of(1900, 1, 1);
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getUsername(),
            testUser.getInstitution(),
            testUser.getPhone(),
            testUser.getAddress(),
            minDate
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
        assertEquals(minDate.toString(), testUser.getDateOfBirth());
    }

    @Test
    void updateProfile_specialCharactersInAllFields_returnsTrue() throws SQLException {
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            "Ann-Marie",
            "O'Connor",
            "ann.marie_123",
            "St. Mary's University",
            "+1-555-123-4567",
            "123 St. Patrick's Ave, Apt #45B",
            LocalDate.parse("1990-01-01")
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
    }

    @Test
    void updateProfile_fieldsWithNumbers_returnsTrue() throws SQLException {
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(),
            testUser.getLastName(),
            "user123abc",
            "University 2024",
            "1234567890",
            "123 Street Name 456",
            LocalDate.parse("1990-01-01")
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
    }

    @Test
    void updateProfile_maxLengthFields_returnsTrue() throws SQLException {
        String longName = "A".repeat(50);
        String longUsername = "u".repeat(30);
        String longInstitution = "University ".repeat(10);
        String longPhone = "1".repeat(20);
        String longAddress = "Very long address ".repeat(20);

        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            longName,
            longName,
            longUsername,
            longInstitution,
            longPhone,
            longAddress,
            LocalDate.parse("1990-01-01")
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
    }

    @Test
    void updateProfile_nullDateOfBirthThenSet_returnsTrue() throws SQLException {
        testUser.setDateOfBirth(null);

        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getUsername(),
            testUser.getInstitution(),
            testUser.getPhone(),
            testUser.getAddress(),
            LocalDate.parse("1995-12-25")
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
        assertEquals("1995-12-25", testUser.getDateOfBirth());
    }

    @Test
    void updateProfile_setDateOfBirthToNull_returnsTrue() throws SQLException {
        ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getUsername(),
            testUser.getInstitution(),
            testUser.getPhone(),
            testUser.getAddress(),
            null
        );

        boolean result = service.updateProfile(testUser, request);
        assertTrue(result);
        assertEquals("", testUser.getDateOfBirth());
    }

}