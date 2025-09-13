import com.cab302.peerpractice.Model.BcryptHasher;
import com.cab302.peerpractice.Model.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HasherTest {

    private PasswordHasher hasher;

    @BeforeEach
    public void setUp() {
        hasher = new BcryptHasher();
    }

    @Test
    public void testHashingAndMatching() {
        String rawPassword = "password";
        String hash = hasher.hasher(rawPassword);

        // hash should not be null or empty
        assertNotNull(hash);
        assertFalse(hash.isEmpty());

        // hash should not equal the raw password
        assertNotEquals(rawPassword, hash);

        // the same password should match the hash
        assertTrue(hasher.matches(rawPassword,hash));

        // a different password should not match
        assertFalse(hasher.matches("wrongpassword",hash ));
    }
}
