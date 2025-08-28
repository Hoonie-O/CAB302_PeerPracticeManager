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
    private String EMAIL = "seiji@email.com";
    private String EMAIL2 = "jack@email.com";
    private String INSTITUTION = "QUT";
    private String INSTITUTION2 = "UQ";
    private char[] PASSWORD = {'e','x','a','m','p','l','e'};
    private char[] PASSWORD2 = {'1','2','3','4','5','6'};
    private String BIO = "Im a student";

    private User user;
    private User user2;
    private User user3;

    @BeforeEach
    public void setUp(){
        user = new User(FIRST_NAME,LAST_NAME,EMAIL,PASSWORD,INSTITUTION);
        user2 = new User(FIRST_NAME2,LAST_NAME2,EMAIL2,PASSWORD2,INSTITUTION2);
        user3 = new User("Bon","Jovi","bonjovi@email.com","hello","UNSW");
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
        assertEquals(null,user.getFriendList());
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

    /*
     * Bio should be max 200 chars
    */
    @Test
    public void testSetBioMaxChar(){
        String longBio = "k9fLQ2zXv3M1pRt8NwYbUc7Dh4jSaZ5Vg0eHxTiOqLmBnCrKsPuJdFoGlWyEnMbR7t6p5q4z3x2c1v0a9s8d7f6g5h4j3k2l1m0n9o8p7QWERTYuiopASDFghjkLZXCVbnm1234567890!@#$%^&*()_+[]{}|;:,.<>?";
        assertThrows(IllegalArgumentException.class, ()-> user.setBio(longBio));
    }




}
