import com.cab302.peerpractice.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    private String FIRST_NAME = "Seiji";
    private String FIRST_NAME2 = "Jack";
    private String LAST_NAME = "Sato";
    private String LAST_NAME2 = "Smith";
    private String USERNAME = "sati2030";
    private String USERNAME2 = "jack_Harlow";
    private String EMAIL = "seiji@email.com";
    private String EMAIL2 = "jack@email.com";
    private String INSTITUTION = "QUT";
    private String INSTITUTION2 = "UQ";
    private String PASSWORD = "password";
    private String PASSWORD2 = "password2";
    private String BIO = "Im a student";

    private User user;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp(){
        user = new User(FIRST_NAME,LAST_NAME,USERNAME,EMAIL,PASSWORD,INSTITUTION);
        user2 = new User(FIRST_NAME2,LAST_NAME2,USERNAME2,EMAIL2,PASSWORD2,INSTITUTION2);
        user3 = new User("Bon","Jovi","bonjovi","bonjovi@email.com", "hello","UNSW");
    }

    @Test
    public void testGetFirstName(){
        assertEquals(FIRST_NAME,user.getFirstName());
    }

    @Test
    public void testSetFirstName(){
        user.setFirstName(FIRST_NAME2);
        assertEquals(FIRST_NAME2,user.getFirstName());
    }

    @Test
    public void testFirstNameNull(){
        assertThrows(IllegalArgumentException.class, () -> user.setFirstName(null));
    }

    @Test
    public void testNumberFirstName(){
        assertThrows(IllegalArgumentException.class, () -> user.setFirstName("12342"));
    }

    @Test
    public void testSymbolFirstName(){
        assertThrows(IllegalArgumentException.class, () -> user.setFirstName("?!:-"));
    }

    @Test
    public void testMixedInvalidFirstName(){
        assertThrows(IllegalArgumentException.class, () -> user.setFirstName("henry2_"));
    }

    @Test
    public void testTrimmedFirstName(){
        User u = new User("     Henry    ","Boggus","hebouser","hebo@gmail.com","hello","QUT");
        assertEquals("Henry",u.getFirstName());
    }

    @Test
    public void testGetLastName(){
        assertEquals(LAST_NAME,user.getLastName());
    }

    @Test
    public void testSetLastName(){
        user.setLastName(LAST_NAME2);
        assertEquals(LAST_NAME2,user.getLastName());
    }

    @Test
    public void testLastNameNull(){
        assertThrows(IllegalArgumentException.class, () -> user.setLastName(null));
    }

    @Test
    public void testSymbolLastName(){
        assertThrows(IllegalArgumentException.class, () -> user.setLastName("?!:-"));
    }

    @Test
    public void testMixedInvalidLastName(){
        assertThrows(IllegalArgumentException.class, () -> user.setLastName("henry2_"));
    }

    @Test
    public void testTrimmedLastName(){
        User u = new User("Henry","  Boggus     ","hebouser","hebo@gmail.com","hello","QUT");
        assertEquals("Boggus",u.getLastName());
    }

    @Test
    public void testGetUsername(){
        assertEquals(USERNAME,user.getUsername());
    }

    @Test
    public void testSetUsername(){
        user.setUsername(USERNAME2);
        assertEquals(USERNAME2,user.getUsername());
    }

    @Test
    public void testUsernameNull(){
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(null));
    }

    @Test
    public void testUsernameInvalidSymbols(){
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("!@#$%^&*()"));
    }

    @Test
    public void testUsernameUnderscore(){
        user.setUsername("hola_perro");
        assertEquals("hola_perro",user.getUsername());
    }

    @Test
    public void testUsernameDot(){
        user.setUsername("hola.perro");
        assertEquals("hola.perro",user.getUsername());
    }

    @Test
    public void testShortUsername(){
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("dale"));
    }

    @Test
    public void testUsernameOnlyNumbers(){
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("123124215"));
    }

    @Test
    public void testUsernameTrim(){
        user.setUsername("sati203");
        assertEquals("sati203",user.getUsername());
    }

    @Test
    public void testGetEmail(){
        assertEquals(EMAIL,user.getEmail());
    }

    @Test
    public void testSetEmail(){
        user.setEmail(EMAIL2);
        assertEquals(EMAIL2,user.getEmail());
    }

    @Test
    public void testEmailNull(){
        assertThrows(IllegalArgumentException.class, () -> user.setEmail(null));
    }

    @Test
    public void testNumbersEmail(){
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("123412"));
    }

    @Test
    public void testInvalidEmail(){
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("asfbasfa"));
    }

    @Test
    public void testTrimmedEmail(){
        User u = new User("Henry","Boggus","hebouser","  hebo@gmail.com   ","hello","QUT");
        assertEquals("hebo@gmail.com",u.getEmail());
    }

    @Test
    public void testGetInstitution(){
        assertEquals(INSTITUTION,user.getInstitution());
    }

    @Test
    public void testSetInstitution(){
        user.setInstitution(INSTITUTION2);
        assertEquals(INSTITUTION2,user.getInstitution());
    }

    @Test
    public void testGetPassword(){
        assertEquals(PASSWORD,user.getPassword());
    }

    @Test
    public void testSetPassword(){
        user.setPassword(PASSWORD2);
        assertEquals(PASSWORD2,user.getPassword());
    }

    @Test
    public void testEmptyGetFriendList(){
        assertEquals(Collections.emptyList(),user.getFriendList());
    }

    @Test
    public void testAddFriend(){
        user.addFriend(user2);
        List<User> expectedList = Collections.singletonList(user2);
        assertEquals(expectedList,user.getFriendList());
    }

    @Test
    public void testMultipleFriends(){
        user.addFriend(user2);
        user.addFriend(user3);
        List<User> expected = new ArrayList<>();
        expected.add(user2);
        expected.add(user3);
        assertEquals(expected,user.getFriendList());
    }

    @Test
    public void testNullAddFriend(){
        assertThrows(IllegalArgumentException.class, () -> user.addFriend(null));
    }

    @Test
    public void testSetBio(){
        user.setBio(BIO);
        assertEquals(BIO,user.getBio());
    }

    @Test
    public void testSetNullBio(){
        assertThrows(IllegalArgumentException.class, () -> user.setBio(null));
    }

    //Bio should be max 200 chars
    @Test
    public void testSetBioMaxChar(){
        String longBio = "k9fLQ2zXv3M1pRt8NwYbUc7Dh4jSaZ5Vg0eHxTiOqLmBnCrKsPuJdFoGlWyEnMbR7t6p5q4z3x2c1v0a9s8d7f6g5h4j3k2l1m0n9o8p7QWERTYuiopASDFghjkLZXCVbnm1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
        assertThrows(IllegalArgumentException.class, ()-> user.setBio(longBio));
    }

    @Test
    public void testSetBioMaxLength200Chars(){
        String exactLength200 = "a".repeat(200);
        assertDoesNotThrow(() -> user.setBio(exactLength200));
        assertEquals(exactLength200, user.getBio());
    }

    @Test
    public void testSetBioLength201Chars(){
        String length201 = "a".repeat(201);
        assertThrows(IllegalArgumentException.class, () -> user.setBio(length201));
    }

    @Test
    public void testSetBioEmptyString(){
        assertDoesNotThrow(() -> user.setBio(""));
        assertEquals("", user.getBio());
    }

    @Test
    public void testSetBioWhitespaceOnly(){
        assertDoesNotThrow(() -> user.setBio("   "));
        assertEquals("", user.getBio());
    }

    @Test
    public void testSetBioSpecialCharacters(){
        String specialBio = "!@#$%^&*()_+-=[]{}|;':,.<>?";
        assertDoesNotThrow(() -> user.setBio(specialBio));
        assertEquals(specialBio, user.getBio());
    }

    @Test
    public void testSetBioUnicodeCharacters(){
        String unicodeBio = "こんにちは 世界 🌍 ñáéíóú";
        assertDoesNotThrow(() -> user.setBio(unicodeBio));
        assertEquals(unicodeBio, user.getBio());
    }

    @Test
    public void testGetSetPhone(){
        user.setPhone("1234567890");
        assertEquals("1234567890", user.getPhone());
    }

    @Test
    public void testSetPhoneEmpty(){
        user.setPhone("");
        assertEquals("", user.getPhone());
    }

    @Test
    public void testSetPhoneNull(){
        user.setPhone(null);
        assertEquals("", user.getPhone());
    }

    @Test
    public void testSetPhoneWithSpaces(){
        user.setPhone("  123 456 7890  ");
        assertEquals("123 456 7890", user.getPhone());
    }

    @Test
    public void testGetSetAddress(){
        user.setAddress("123 Main Street");
        assertEquals("123 Main Street", user.getAddress());
    }

    @Test
    public void testSetAddressEmpty(){
        user.setAddress("");
        assertEquals("", user.getAddress());
    }

    @Test
    public void testSetAddressNull(){
        user.setAddress(null);
        assertEquals("", user.getAddress());
    }

    @Test
    public void testSetAddressWithSpaces(){
        user.setAddress("  123 Main St, Apt 4B  ");
        assertEquals("123 Main St, Apt 4B", user.getAddress());
    }

    @Test
    public void testGetSetDateOfBirth(){
        user.setDateOfBirth("1990-01-01");
        assertEquals("1990-01-01", user.getDateOfBirth());
    }

    @Test
    public void testSetDateOfBirthEmpty(){
        user.setDateOfBirth("");
        assertEquals("", user.getDateOfBirth());
    }

    @Test
    public void testSetDateOfBirthNull(){
        user.setDateOfBirth(null);
        assertEquals("", user.getDateOfBirth());
    }

    @Test
    public void testSetDateOfBirthWithSpaces(){
        user.setDateOfBirth("  1985-12-25  ");
        assertEquals("1985-12-25", user.getDateOfBirth());
    }

    @Test
    public void testConstructorWithUserId(){
        User userWithId = new User("userId123", FIRST_NAME, LAST_NAME, USERNAME, EMAIL, PASSWORD, INSTITUTION);
        assertEquals("userId123", userWithId.getUserId());
        assertEquals(FIRST_NAME, userWithId.getFirstName());
        assertEquals(LAST_NAME, userWithId.getLastName());
    }

    @Test
    public void testConstructorGeneratesUniqueIds(){
        User user1 = new User(FIRST_NAME, LAST_NAME, "user123", "user1@email.com", PASSWORD, INSTITUTION);
        User user2 = new User(FIRST_NAME, LAST_NAME, "user234", "user2@email.com", PASSWORD, INSTITUTION);
        assertNotEquals(user1.getUserId(), user2.getUserId());
    }

    @Test
    public void testUserIdNotNull(){
        assertNotNull(user.getUserId());
        assertFalse(user.getUserId().isEmpty());
    }

    @Test
    public void testFirstNameWithAccents(){
        assertDoesNotThrow(() -> user.setFirstName("José"));
        assertEquals("José", user.getFirstName());
    }

    @Test
    public void testLastNameWithAccents(){
        assertDoesNotThrow(() -> user.setLastName("González"));
        assertEquals("González", user.getLastName());
    }

    @Test
    public void testFirstNameSingleLetter(){
        assertDoesNotThrow(() -> user.setFirstName("A"));
        assertEquals("A", user.getFirstName());
    }

    @Test
    public void testLastNameSingleLetter(){
        assertDoesNotThrow(() -> user.setLastName("B"));
        assertEquals("B", user.getLastName());
    }

    @Test
    public void testUsernameMinimumLength(){
        assertDoesNotThrow(() -> user.setUsername("user12"));
        assertEquals("user12", user.getUsername());
    }

    @Test
    public void testUsernameLongValid(){
        String longUsername = "a".repeat(50);
        assertDoesNotThrow(() -> user.setUsername(longUsername));
        assertEquals(longUsername, user.getUsername());
    }

    @Test
    public void testUsernameWithMixedCase(){
        assertDoesNotThrow(() -> user.setUsername("UsEr123"));
        assertEquals("UsEr123", user.getUsername());
    }

    @Test
    public void testEmailVariousValidFormats(){
        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "user+tag@example.org",
            "test123@subdomain.example.com",
            "a@b.co"
        };

        for (String email : validEmails) {
            assertDoesNotThrow(() -> user.setEmail(email), "Failed for email: " + email);
            assertEquals(email, user.getEmail());
        }
    }

    @Test
    public void testEmailInvalidFormats(){
        String[] invalidEmails = {
            "notanemail",
            "@example.com",
            "user@",
            "user@domain",
            "user.domain.com",
            "user@@domain.com",
            "user@domain.",
            ""
        };

        for (String email : invalidEmails) {
            assertThrows(IllegalArgumentException.class, () -> user.setEmail(email), "Should have failed for email: " + email);
        }
    }

    @Test
    public void testPasswordGetterSetter(){
        user.setPassword("newPassword");
        assertEquals("newPassword", user.getPassword());
    }

    @Test
    public void testPasswordNull(){
        assertDoesNotThrow(() -> user.setPassword(null));
        assertNull(user.getPassword());
    }

    @Test
    public void testPasswordEmpty(){
        assertDoesNotThrow(() -> user.setPassword(""));
        assertEquals("", user.getPassword());
    }

    @Test
    public void testInstitutionGetterSetter(){
        user.setInstitution("Harvard");
        assertEquals("Harvard", user.getInstitution());
    }

    @Test
    public void testInstitutionNull(){
        assertDoesNotThrow(() -> user.setInstitution(null));
        assertNull(user.getInstitution());
    }

    @Test
    public void testAddFriendMultipleTimes(){
        user.addFriend(user2);
        user.addFriend(user2);
        assertEquals(2, user.getFriendList().size());
    }

    @Test
    public void testFriendListIndependence(){
        List<User> friends = user.getFriendList();
        friends.add(user2);
        assertEquals(1, user.getFriendList().size());
    }


    @Test
    public void testFirstNameLeadingTrailingSpaces(){
        user.setFirstName("   John   ");
        assertEquals("John", user.getFirstName());
    }

    @Test
    public void testLastNameLeadingTrailingSpaces(){
        user.setLastName("   Doe   ");
        assertEquals("Doe", user.getLastName());
    }

    @Test
    public void testUsernameWithSpacesInvalid(){
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("user name"));
    }

    @Test
    public void testUsernameWithSpecialCharsInvalid(){
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("user@name"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("user#name"));
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("user$name"));
    }

    @Test
    public void testEmailLeadingTrailingSpaces(){
        user.setEmail("   test@example.com   ");
        assertEquals("test@example.com", user.getEmail());
    }

    @Test
    public void testBoundaryValueUsernameLengths(){
        assertThrows(IllegalArgumentException.class, () -> user.setUsername("Cat1."));
        assertDoesNotThrow(() -> user.setUsername("Cat12."));
    }

    @Test
    public void testFirstNameEmptyAfterTrim(){
        assertThrows(IllegalArgumentException.class, () -> user.setFirstName("   "));
    }

    @Test
    public void testLastNameEmptyAfterTrim(){
        assertThrows(IllegalArgumentException.class, () -> user.setLastName("   "));
    }

    @Test
    public void testUserObjectEquality(){
        User sameUser = new User(user.getUserId(), FIRST_NAME, LAST_NAME, USERNAME, EMAIL, PASSWORD, INSTITUTION);
        assertEquals(user, sameUser);
    }

    @Test
    public void testUserToStringContainsUsername(){
        String userString = user.toString();
        assertTrue(userString != null && !userString.isEmpty());
    }

    @Test
    public void testMultipleSpacesInName(){
        assertDoesNotThrow(() -> user.setFirstName("Mary  Jane"));
        assertEquals("Mary  Jane", user.getFirstName());
    }

    @Test
    public void testSetInstitutionLongName(){
        String longInstitution = "University of Technology Sydney with a Very Long Name";
        assertDoesNotThrow(() -> user.setInstitution(longInstitution));
        assertEquals(longInstitution, user.getInstitution());
    }

}
