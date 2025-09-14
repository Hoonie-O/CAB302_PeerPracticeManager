import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.InvalidPasswordException;
import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.print.attribute.standard.MediaSize;
import java.io.FileReader;
import java.util.Optional;

import static javafx.beans.binding.Bindings.when;
import static org.junit.jupiter.api.Assertions.*;

public class UserManagerTest {

    private IUserDAO userdao;
    private UserManager manager;
    private PasswordHasher hasher;
    private String NAME = "Seiji";
    private String LASTNAME = "Sato";
    private String INSTITUTION = "QUT";
    private String PASSWORD = "PrY123?12";
    private String EMAIL = "email@email.com";
    private String USERNAME = "sati2030";
    private final String HASHED_PASSWORD = "$2a$HASHED";
    private User user;


    @BeforeEach
    public void setUp(){
        userdao = new MockDAO();
        hasher = new BcryptHasher();
        manager = new UserManager(userdao, hasher);
        user = new User(NAME,LASTNAME,USERNAME,EMAIL,"$oldHash",INSTITUTION);
    }

    @Test
    public void signUp_Normal(){
        try {
            assertTrue(manager.signUp(NAME, LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION));
        }catch(Exception e){
            fail();
        }
    }

    @Test
    public void signUp_Email_Exists(){
        try {
            manager.signUp(NAME, LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION);
        }catch(Exception e){
            fail();
        }
        assertThrows(DuplicateEmailException.class, () -> manager.signUp(NAME,LASTNAME,"asss",EMAIL,PASSWORD,INSTITUTION));
    }

    @Test
    public void signUp_usernameExists(){
        try {
            manager.signUp(NAME, LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION);
        }catch(Exception e){
            fail();
        }
        assertThrows(DuplicateUsernameException.class, () -> manager.signUp(NAME,LASTNAME,USERNAME,"other@other.com",PASSWORD,INSTITUTION));
    }

    @Test
    void signUp_usernameNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, null, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "", EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "   ", EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameTooShort_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "abc", EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameAllDigits_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "123456", EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameContainsSpace_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice 01", EMAIL, PASSWORD, INSTITUTION)
        );
    }


    @Test
    void signUp_usernameContainsDash_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice-01", EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameContainsExclamation_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice!01", EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameContainsTab_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "alice\t01", EMAIL, PASSWORD, INSTITUTION)
        );
    }


    @Test
    void signUp_usernameNonAsciiLetters_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, "álìçé01", EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_usernameValid_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp(NAME, LASTNAME, "alice_01", EMAIL, PASSWORD, INSTITUTION)
        );
    }


    @Test
    void changePassword_passwordNull_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, null)
        );
    }

    @Test
    void changePassword_passwordEmpty_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, "")
        );
    }

    @Test
    void changePassword_passwordTooShort_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, "Ab1!") // 4 chars < 8
        );
    }

    @Test
    void changePassword_passwordTooLong_throws() {
        String tooLong = "A1!" + "a".repeat(100); // > 72 chars
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, tooLong)
        );
    }

    @Test
    void changePassword_noUppercase_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, "lowercase1!")
        );
    }

    @Test
    void changePassword_noLowercase_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, "ALLUPPER1!")
        );
    }

    @Test
    void changePassword_noDigit_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, "NoDigit!")
        );
    }

    @Test
    void changePassword_noSpecialCharacter_throws() {
        assertThrows(InvalidPasswordException.class, () ->
                manager.changePassword(user, "NoSpecial1")
        );
    }

    @Test
    void changePassword_validPassword_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.changePassword(user, "Valid1!")
        );
    }

    @Test
    void signUp_emailNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, null, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_emailEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_emailBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "   ", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_emailMissingAtSymbol_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice.example.com", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_emailMissingDomain_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice@", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_emailMissingTopLevelDomain_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice@example", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_emailInvalidCharacters_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "ali ce@example.com", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_emailTooShortTld_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice@example.c", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_validEmail_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice@example.com", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_validEmailWithPlusAndDots_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp(NAME, LASTNAME, USERNAME, "alice.smith+test@example.co.uk", PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(null, LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameEmpty_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("", LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("   ", LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameWithSpace_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("Alice Marie", LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameWithHyphen_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("Mary-Jane", LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameWithApostrophe_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("O'Neil", LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameWithDigit_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("John2", LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_firstNameWithPunctuation_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp("Alice!", LASTNAME, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_lastNameNull_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, null, USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_lastNameBlankOnly_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, "   ", USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_lastNameWithHyphen_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, "Smith-Jones", USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_lastNameWithApostrophe_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                manager.signUp(NAME, "O'Reilly", USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_validAsciiNames_doesNotThrow() {
        assertDoesNotThrow(() ->
                manager.signUp("Alice", "Smith", USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }

    @Test
    void signUp_validUnicodeNames_doesNotThrow() {
        // \p{L} allows Unicode letters; should pass under your rule
        assertDoesNotThrow(() ->
                manager.signUp("José", "Łukasz", USERNAME, EMAIL, PASSWORD, INSTITUTION)
        );
    }


}
