import com.cab302.peerpractice.Model.BcryptHasher;
import com.cab302.peerpractice.Model.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HasherTest {

    private PasswordHasher hasher;
    private String rawPassword;
    private String hash;

    @BeforeEach
    public void setUp() {
        hasher = new BcryptHasher();
        rawPassword = "password";
        hash = hasher.hasher(rawPassword);
    }

    @Test
    public void testHashIsGenerated() {
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertNotEquals(rawPassword, hash);
    }

    @Test
    public void testCorrectPasswordMatches() {
        assertTrue(hasher.matches(rawPassword, hash));
    }

    @Test
    public void testWrongPasswordDoesNotMatch() {
        assertFalse(hasher.matches("wrongpassword", hash));
    }

    @Test
    public void testInvalidHashFormatReturnsFalse() {
        assertFalse(hasher.matches(rawPassword, "notAHash"));
    }

    @Test
    public void testNullHashReturnsFalse() {
        assertFalse(hasher.matches(rawPassword, null));
    }

    @Test
    public void testEmptyHashReturnsFalse() {
        assertFalse(hasher.matches(rawPassword, ""));
    }
}
