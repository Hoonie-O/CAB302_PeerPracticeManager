import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.InvalidPasswordException;
import com.cab302.peerpractice.Model.daos.IUserDAO;
import com.cab302.peerpractice.Model.daos.UserDAO;
import com.cab302.peerpractice.Model.entities.User;
import com.cab302.peerpractice.Model.managers.UserManager;
import com.cab302.peerpractice.Model.utils.BcryptHasher;
import com.cab302.peerpractice.Model.utils.PasswordHasher;
import org.junit.jupiter.api.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    private IUserDAO userdao;
    private UserManager manager;
    private PasswordHasher hasher;
    private final String NAME = "Seiji";
    private final String LASTNAME = "Sato";
    private final String INSTITUTION = "QUT";
    private final String PASSWORD = "PrY123?12";
    private final String EMAIL = "email@email.com";
    private final String USERNAME = "sati2030";
    private User user;

    @BeforeEach
    public void setUp() throws SQLException {
        userdao = new UserDAO();
        hasher = new BcryptHasher();
        manager = new UserManager(userdao, hasher);

        // Clean up possible leftovers from previous test runs
        try { userdao.deleteUser(USERNAME); } catch (Exception ignored) {}
        try { userdao.deleteUser("asssss"); } catch (Exception ignored) {}
        try { userdao.deleteUser("other@other.com"); } catch (Exception ignored) {}

        user = new User(NAME, LASTNAME, USERNAME, EMAIL, "$oldHash", INSTITUTION);
        userdao.addUser(user);
    }

    @AfterEach
    public void tearDown() {
        try { userdao.deleteUser(USERNAME); } catch (Exception ignored) {}
        try { userdao.deleteUser("asssss"); } catch (Exception ignored) {}
        try { userdao.deleteUser("other@other.com"); } catch (Exception ignored) {}
        try { userdao.deleteUser("alice_01"); } catch (Exception ignored) {}
        try { userdao.deleteUser("newusername"); } catch (Exception ignored) {}
    }

    // === SIGN UP TESTS ===

    @Test
    public void signUp_Normal() throws Exception {
        assertTrue(manager.signUp("Alice", "Smith", "aliceuser",
                "alice@example.com", PASSWORD, INSTITUTION));
        // cleanup
        try { userdao.deleteUser("aliceuser"); } catch (Exception ignored) {}
    }

    @Test
    public void signUp_Email_Exists() {
        assertThrows(DuplicateEmailException.class, () ->
                manager.signUp(NAME, LASTNAME, "asssss", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test
    public void signUp_usernameExists() {
        assertThrows(DuplicateUsernameException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "other@other.com", PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, null, EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "   ", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameTooShort_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "abc", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameAllDigits_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "123456", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameContainsSpace_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice 01", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameContainsDash_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice-01", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameContainsExclamation_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice!01", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameContainsTab_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice\t01", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameNonAsciiLetters_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "álìçé01", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_usernameValid_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp(NAME, LASTNAME, "alice_01", EMAIL, PASSWORD, INSTITUTION));
        try { userdao.deleteUser("alice_01"); } catch (Exception ignored) {}
    }

    // === PASSWORD CHANGE TESTS ===

    @Test void changePassword_passwordNull_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), null));
    }

    @Test void changePassword_passwordEmpty_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), ""));
    }

    @Test void changePassword_passwordTooShort_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), "Ab1!"));
    }

    @Test void changePassword_passwordTooLong_throws() {
        String tooLong = "A1!" + "a".repeat(100);
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), tooLong));
    }

    @Test void changePassword_noUppercase_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), "lowercase1!"));
    }

    @Test void changePassword_noLowercase_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), "ALLUPPER1!"));
    }

    @Test void changePassword_noDigit_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), "NoDigit!"));
    }

    @Test void changePassword_noSpecialCharacter_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user.getUsername(), "NoSpecial1"));
    }

    @Test void changePassword_validPassword_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.changePassword(user.getUsername(), "ValidPass1!"));
    }

    // === EMAIL VALIDATION TESTS ===

    @Test void signUp_emailNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, null, PASSWORD, INSTITUTION));
    }

    @Test void signUp_emailEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "", PASSWORD, INSTITUTION));
    }

    @Test void signUp_emailBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "   ", PASSWORD, INSTITUTION));
    }

    @Test void signUp_emailMissingAtSymbol_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice.example.com", PASSWORD, INSTITUTION));
    }

    @Test void signUp_emailMissingDomain_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice@", PASSWORD, INSTITUTION));
    }

    @Test void signUp_emailMissingTopLevelDomain_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice@example", PASSWORD, INSTITUTION));
    }

    @Test void signUp_emailInvalidCharacters_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "ali ce@example.com", PASSWORD, INSTITUTION));
    }

    @Test void signUp_emailTooShortTld_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice@example.c", PASSWORD, INSTITUTION));
    }

    @Test void signUp_validEmail_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp(NAME, LASTNAME, USERNAME + "2", "alice@example.com", PASSWORD, INSTITUTION));
        try { userdao.deleteUser(USERNAME + "2"); } catch (Exception ignored) {}
    }

    @Test void signUp_validEmailWithPlusAndDots_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp(NAME, LASTNAME, USERNAME + "3", "alice.smith+test@example.co.uk", PASSWORD, INSTITUTION));
        try { userdao.deleteUser(USERNAME + "3"); } catch (Exception ignored) {}
    }

    // === NAME VALIDATION TESTS ===

    @Test void signUp_firstNameNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(null, LASTNAME, USERNAME + "4", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_firstNameEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("", LASTNAME, USERNAME + "5", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_firstNameBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("   ", LASTNAME, USERNAME + "6", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_firstNameWithDigit_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("John2", LASTNAME, USERNAME + "7", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_firstNameWithPunctuation_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("Alice!", LASTNAME, USERNAME + "8", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_lastNameNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, null, USERNAME + "9", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_lastNameBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, "   ", USERNAME + "10", EMAIL, PASSWORD, INSTITUTION));
    }

    @Test void signUp_validAsciiNames_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp("Alice", "Smith", USERNAME + "11", EMAIL, PASSWORD, INSTITUTION));
        try { userdao.deleteUser(USERNAME + "11"); } catch (Exception ignored) {}
    }

    @Test void signUp_validUnicodeNames_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp("José", "Łukasz", USERNAME + "12", EMAIL, PASSWORD, INSTITUTION));
        try { userdao.deleteUser(USERNAME + "12"); } catch (Exception ignored) {}
    }
}