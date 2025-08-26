import com.cab302.peerpractice.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    private User user;
    private User user2;

    @BeforeEach
    public void setUp(){
        user = new User(FIRST_NAME,LAST_NAME,EMAIL,INSTITUTION);
        user2 = new User(FIRST_NAME2,LAST_NAME2,EMAIL2,INSTITUTION2);
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
    public void testGetLastName(){
        assertEquals(LAST_NAME,user.getLastName());
    }

    @Test
    public void testSetLastName(){
        user.setLastName(LAST_NAME2);
        assertEquals(LAST_NAME2,user.getLastName());
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

    }





}
