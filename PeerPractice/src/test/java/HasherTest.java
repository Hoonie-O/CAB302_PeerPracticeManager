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

    @Test
    public void testNullPasswordReturnsFalse() {
        assertFalse(hasher.matches(null, hash));
    }

    @Test
    public void testEmptyPasswordHandling() {
        String emptyPasswordHash = hasher.hasher("");
        assertTrue(hasher.matches("", emptyPasswordHash));
        assertFalse(hasher.matches("notempty", emptyPasswordHash));
    }

    @Test
    public void testConsistentHashing() {
        String password = "testpassword123";
        String hash1 = hasher.hasher(password);
        String hash2 = hasher.hasher(password);

        assertNotEquals(hash1, hash2);
        assertTrue(hasher.matches(password, hash1));
        assertTrue(hasher.matches(password, hash2));
    }

    @Test
    public void testComplexPasswordHashing() {
        String complexPassword = "P@ssw0rd!2024_with_Special-Ch@rs";
        String complexHash = hasher.hasher(complexPassword);

        assertTrue(hasher.matches(complexPassword, complexHash));
        assertFalse(hasher.matches("P@ssw0rd!2024_with_Special-Ch@r", complexHash));
    }

    @Test
    public void testUnicodePasswordHashing() {
        String unicodePassword = "你好世界🌍Привет";
        String unicodeHash = hasher.hasher(unicodePassword);

        assertTrue(hasher.matches(unicodePassword, unicodeHash));
        assertFalse(hasher.matches("你好世界", unicodeHash));
    }

    @Test
    public void testLongPasswordHashing() {
        String longPassword = "a".repeat(100) + "B1!";
        String longHash = hasher.hasher(longPassword);

        assertTrue(hasher.matches(longPassword, longHash));
        // BCrypt truncates at 72 bytes, so test with different first 70 chars
        assertFalse(hasher.matches("b".repeat(70) + "B1!", longHash));
    }

    @Test
    public void testWhitespaceInPassword() {
        String whitespacePassword = "  password with spaces  ";
        String whitespaceHash = hasher.hasher(whitespacePassword);

        assertTrue(hasher.matches(whitespacePassword, whitespaceHash));
        assertFalse(hasher.matches("password with spaces", whitespaceHash));
        assertFalse(hasher.matches(whitespacePassword.trim(), whitespaceHash));
    }

    @Test
    public void testSpecialCharactersPassword() {
        String[] specialPasswords = {
            "pass!@#$%^&*()",
            "pass[]{}|\\:;'\"<>?,./~`",
            "pass\t\n\r",
            "pass ©®™"
        };

        for (String password : specialPasswords) {
            String hash = hasher.hasher(password);
            assertTrue(hasher.matches(password, hash), "Failed for password: " + password);
        }
    }

    @Test
    public void testCaseSensitiveMatching() {
        String lowerPassword = "password";
        String upperPassword = "PASSWORD";
        String mixedPassword = "Password";

        String lowerHash = hasher.hasher(lowerPassword);

        assertTrue(hasher.matches(lowerPassword, lowerHash));
        assertFalse(hasher.matches(upperPassword, lowerHash));
        assertFalse(hasher.matches(mixedPassword, lowerHash));
    }

    @Test
    public void testHashFormatValidation() {
        String[] invalidHashes = {
            "plaintext",
            "$2a$invalid",
            "$2b$10$invalid",
            "$invalid$format",
            "tooshort",
            "$2a$10$short"
        };

        for (String invalidHash : invalidHashes) {
            assertFalse(hasher.matches(rawPassword, invalidHash),
                "Should not match invalid hash: " + invalidHash);
        }
    }

    @Test
    public void testHashAlgorithmConsistency() {
        String testPassword = "consistencytest123!";
        String hash = hasher.hasher(testPassword);

        assertTrue(hash.startsWith("$2a$"), "Hash should use BCrypt format");
        assertTrue(hash.length() >= 60, "BCrypt hash should be at least 60 characters");
    }

    @Test
    public void testMultipleHashersIndependence() {
        PasswordHasher hasher1 = new BcryptHasher();
        PasswordHasher hasher2 = new BcryptHasher();

        String password = "independence_test";
        String hash1 = hasher1.hasher(password);
        String hash2 = hasher2.hasher(password);

        assertNotEquals(hash1, hash2);
        assertTrue(hasher1.matches(password, hash1));
        assertTrue(hasher2.matches(password, hash2));
        assertTrue(hasher1.matches(password, hash2));
        assertTrue(hasher2.matches(password, hash1));
    }

    @Test
    public void testPasswordWithNewlines() {
        String passwordWithNewlines = "password\nwith\nnewlines";
        String hash = hasher.hasher(passwordWithNewlines);

        assertTrue(hasher.matches(passwordWithNewlines, hash));
        assertFalse(hasher.matches("password with newlines", hash));
    }

    @Test
    public void testPasswordWithTabs() {
        String passwordWithTabs = "password\twith\ttabs";
        String hash = hasher.hasher(passwordWithTabs);

        assertTrue(hasher.matches(passwordWithTabs, hash));
        assertFalse(hasher.matches("password with tabs", hash));
    }

    @Test
    public void testNumericPassword() {
        String numericPassword = "1234567890";
        String hash = hasher.hasher(numericPassword);

        assertTrue(hasher.matches(numericPassword, hash));
        assertFalse(hasher.matches("0987654321", hash));
    }

    @Test
    public void testSingleCharacterPassword() {
        String singleChar = "a";
        String hash = hasher.hasher(singleChar);

        assertTrue(hasher.matches(singleChar, hash));
        assertFalse(hasher.matches("A", hash));
        assertFalse(hasher.matches("b", hash));
    }

    @Test
    public void testBoundaryPasswordLengths() {
        String shortPassword = "ab";
        String mediumPassword = "a".repeat(50);
        String longPassword = "a".repeat(200);

        String shortHash = hasher.hasher(shortPassword);
        String mediumHash = hasher.hasher(mediumPassword);
        String longHash = hasher.hasher(longPassword);

        assertTrue(hasher.matches(shortPassword, shortHash));
        assertTrue(hasher.matches(mediumPassword, mediumHash));
        assertTrue(hasher.matches(longPassword, longHash));

        assertFalse(hasher.matches(shortPassword, mediumHash));
        assertFalse(hasher.matches(mediumPassword, longHash));
        assertFalse(hasher.matches(longPassword, shortHash));
    }

    @Test
    public void testHashingPerformance() {
        String password = "performance_test_password";

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            hasher.hasher(password + i);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        assertTrue(duration < 5000, "Hashing 10 passwords should take less than 5 seconds");
    }

    @Test
    public void testMatchingPerformance() {
        String password = "matching_test_password";
        String hash = hasher.hasher(password);

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            hasher.matches(password, hash);
        }
        long endTime = System.currentTimeMillis();

        long duration = endTime - startTime;
        assertTrue(duration < 10000, "Matching 100 passwords should take less than 10 seconds");
    }

}
