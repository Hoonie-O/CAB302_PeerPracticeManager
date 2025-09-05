import com.cab302.peerpractice.Model.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class HasherTest {

    private PasswordHasher hasher;

    //Change for current implementation of hasher
    @BeforeEach
    public void setUp(){
        hasher = new bcrypt();
    }

    @Test
    public void testHashing(){
        assertEquals("$2a$12$mYPDrFpdRS9XODR8EEPouus7jZGrdJjGTbSKE4w6JArohOT7jhbZy",hasher.hash("password"));
    }

    @Test
    public void testVerifyTrue(){
        assertEquals(true,hasher.verify("$2a$12$mYPDrFpdRS9XODR8EEPouus7jZGrdJjGTbSKE4w6JArohOT7jhbZy","password"));
    }

    @Test
    public void testVerifyFalse(){
        assertEquals(false,hasher.verify("$2a$12$mYPDrFpdRS9XODR8EEPouus7jZGrdJjGTbSKE4w6JArohOT7jhbZy","password2"));
    }


}
