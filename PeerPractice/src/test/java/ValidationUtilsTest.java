import com.cab302.peerpractice.Model.Utils.ValidationUtils;
import com.cab302.peerpractice.Exceptions.InvalidPasswordException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilsTest {

    @Test
    void validateAndCleanName_validName_returnsCleanedName() {
        String result = ValidationUtils.validateAndCleanName("  John  ", "First name");
        assertEquals("John", result);
    }

    @Test
    void validateAndCleanName_nullName_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanName(null, "First name"));
    }

    @Test
    void validateAndCleanName_emptyName_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanName("   ", "First name"));
    }

    @Test
    void validateAndCleanName_nameWithNumbers_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanName("John123", "First name"));
    }

    @Test
    void validateAndCleanName_nameWithSpecialChars_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanName("John@", "First name"));
    }

    @Test
    void validateAndCleanName_nameWithSpaces_valid() {
        String result = ValidationUtils.validateAndCleanName("Mary Jane", "First name");
        assertEquals("Mary Jane", result);
    }

    @Test
    void validateAndCleanUsername_validUsername_returnsCleanedUsername() {
        String result = ValidationUtils.validateAndCleanUsername("  user123  ");
        assertEquals("user123", result);
    }

    @Test
    void validateAndCleanUsername_shortUsername_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanUsername("abc"));
    }

    @Test
    void validateAndCleanUsername_nullUsername_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanUsername(null));
    }

    @Test
    void validateAndCleanUsername_onlyNumbers_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanUsername("123456"));
    }

    @Test
    void validateAndCleanUsername_validWithUnderscore_passes() {
        String result = ValidationUtils.validateAndCleanUsername("user_123");
        assertEquals("user_123", result);
    }

    @Test
    void validateAndCleanUsername_validWithDot_passes() {
        String result = ValidationUtils.validateAndCleanUsername("user.123");
        assertEquals("user.123", result);
    }

    @Test
    void validateAndCleanEmail_validEmail_returnsCleanedEmail() {
        String result = ValidationUtils.validateAndCleanEmail("  test@example.com  ");
        assertEquals("test@example.com", result);
    }

    @Test
    void validateAndCleanEmail_nullEmail_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanEmail(null));
    }

    @Test
    void validateAndCleanEmail_invalidEmail_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanEmail("invalid-email"));
    }

    @Test
    void validateAndCleanEmail_emailWithoutDomain_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanEmail("test@"));
    }

    @Test
    void validateAndCleanBio_validBio_returnsTrimmedBio() {
        String result = ValidationUtils.validateAndCleanBio("  This is a bio  ");
        assertEquals("This is a bio", result);
    }

    @Test
    void validateAndCleanBio_nullBio_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanBio(null));
    }

    @Test
    void validateAndCleanBio_tooLongBio_throwsException() {
        String longBio = "a".repeat(201);
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanBio(longBio));
    }

    @Test
    void validateAndCleanBio_maxLengthBio_passes() {
        String maxLengthBio = "a".repeat(200);
        String result = ValidationUtils.validateAndCleanBio(maxLengthBio);
        assertEquals(maxLengthBio, result);
    }

    @Test
    void validatePassword_validPassword_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.validatePassword("ValidPass1!"));
    }

    @Test
    void validatePassword_nullPassword_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword(null));
    }

    @Test
    void validatePassword_emptyPassword_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword(""));
    }

    @Test
    void validatePassword_tooShort_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("Pass1!"));
    }

    @Test
    void validatePassword_tooLong_throwsException() {
        String longPassword = "Pass1!" + "a".repeat(100);
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword(longPassword));
    }

    @Test
    void validatePassword_noUppercase_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("validpass1!"));
    }

    @Test
    void validatePassword_noLowercase_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("VALIDPASS1!"));
    }

    @Test
    void validatePassword_noDigit_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("ValidPass!"));
    }

    @Test
    void validatePassword_noSpecialChar_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("ValidPass1"));
    }

    @Test
    void requireNotBlank_validString_doesNotThrow() {
        assertDoesNotThrow(() -> ValidationUtils.requireNotBlank("test", "Test field"));
    }

    @Test
    void requireNotBlank_nullString_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.requireNotBlank(null, "Test field"));
    }

    @Test
    void requireNotBlank_emptyString_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.requireNotBlank("   ", "Test field"));
    }

    @Test
    void validateAndCleanName_unicodeCharacters_valid() {
        String unicodeName = "José María";
        String result = ValidationUtils.validateAndCleanName(unicodeName, "First name");
        assertEquals(unicodeName, result);
    }

    @Test
    void validateAndCleanName_cyrillicCharacters_valid() {
        String cyrillicName = "Александр";
        String result = ValidationUtils.validateAndCleanName(cyrillicName, "First name");
        assertEquals(cyrillicName, result);
    }

    @Test
    void validateAndCleanName_chineseCharacters_valid() {
        String chineseName = "李明";
        String result = ValidationUtils.validateAndCleanName(chineseName, "First name");
        assertEquals(chineseName, result);
    }

    @Test
    void validateAndCleanName_arabicCharacters_valid() {
        String arabicName = "محمد";
        String result = ValidationUtils.validateAndCleanName(arabicName, "First name");
        assertEquals(arabicName, result);
    }

    @Test
    void validateAndCleanName_multipleSpaces_valid() {
        String nameWithSpaces = "Mary    Jane";
        String result = ValidationUtils.validateAndCleanName(nameWithSpaces, "First name");
        assertEquals(nameWithSpaces, result);
    }

    @Test
    void validateAndCleanName_leadingTrailingSpaces_trimmed() {
        String nameWithSpaces = "   John   ";
        String result = ValidationUtils.validateAndCleanName(nameWithSpaces, "First name");
        assertEquals("John", result);
    }

    @Test
    void validateAndCleanName_mixedCaseLetters_valid() {
        String mixedCase = "McDoNALD";
        String result = ValidationUtils.validateAndCleanName(mixedCase, "Last name");
        assertEquals(mixedCase, result);
    }

    @Test
    void validateAndCleanName_singleCharacter_valid() {
        String singleChar = "A";
        String result = ValidationUtils.validateAndCleanName(singleChar, "First name");
        assertEquals(singleChar, result);
    }

    @Test
    void validateAndCleanName_longName_valid() {
        String longName = "Hubert Blaine Wolfeschlegelsteinhausenbergerdorff";
        String result = ValidationUtils.validateAndCleanName(longName, "First name");
        assertEquals(longName, result);
    }

    @Test
    void validateAndCleanUsername_edgeCaseCharacters_valid() {
        String[] validUsernames = {
            "user123",
            "user.name",
            "user_name",
            "User123",
            "USER123",
            "a12345",
            "123abc",
            "user.name.123",
            "user_name_123"
        };

        for (String username : validUsernames) {
            String result = ValidationUtils.validateAndCleanUsername(username);
            assertEquals(username, result);
        }
    }

    @Test
    void validateAndCleanUsername_invalidCharacters_throwsException() {
        String[] invalidUsernames = {
            "user-name",
            "user@name",
            "user#name",
            "user name",
            "user!name",
            "user$name",
            "user%name",
            "user&name"
        };

        for (String username : invalidUsernames) {
            assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validateAndCleanUsername(username),
                "Should have failed for username: " + username);
        }
    }

    @Test
    void validateAndCleanUsername_boundaryLengths_valid() {
        String exactLength6 = "user12";
        String result = ValidationUtils.validateAndCleanUsername(exactLength6);
        assertEquals(exactLength6, result);

        String longUsername = "a".repeat(100);
        String longResult = ValidationUtils.validateAndCleanUsername(longUsername);
        assertEquals(longUsername, longResult);
    }

    @Test
    void validateAndCleanUsername_emptyAfterTrim_throwsException() {
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanUsername("     "));
    }

    @Test
    void validateAndCleanEmail_complexValidEmails_valid() {
        String[] validEmails = {
            "user+tag@domain.com",
            "user.name@subdomain.domain.co.uk",
            "123@domain.com",
            "user@domain-name.com",
            "user@123domain.com",
            "a@b.co",
            "very.long.email.address@very.long.domain.name.com",
            "user123+test@example-domain.org"
        };

        for (String email : validEmails) {
            String result = ValidationUtils.validateAndCleanEmail(email);
            assertEquals(email, result);
        }
    }

    @Test
    void validateAndCleanEmail_invalidFormats_throwsException() {
        String[] invalidEmails = {
            "user@@domain.com",
            "user@domain..com",
            "user@.domain.com",
            "user@domain.c",
            "user@domain.",
            "@domain.com",
            "user@",
            "user.domain.com",
            "user@domain",
            "user @domain.com",
            "user@domain .com",
            "user@domain,com"
        };

        for (String email : invalidEmails) {
            assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.validateAndCleanEmail(email),
                "Should have failed for email: " + email);
        }
    }

    @Test
    void validateAndCleanBio_boundaryValues_valid() {
        String bio199 = "a".repeat(199);
        String result199 = ValidationUtils.validateAndCleanBio(bio199);
        assertEquals(bio199, result199);

        String bio200 = "a".repeat(200);
        String result200 = ValidationUtils.validateAndCleanBio(bio200);
        assertEquals(bio200, result200);
    }

    @Test
    void validateAndCleanBio_exactlyOver200_throwsException() {
        String bio201 = "a".repeat(201);
        assertThrows(IllegalArgumentException.class,
            () -> ValidationUtils.validateAndCleanBio(bio201));
    }

    @Test
    void validateAndCleanBio_unicodeCharacters_valid() {
        String unicodeBio = "Hello 🌍 World 你好 مرحبا Привет";
        String result = ValidationUtils.validateAndCleanBio(unicodeBio);
        assertEquals(unicodeBio, result);
    }

    @Test
    void validateAndCleanBio_newlinesAndTabs_valid() {
        String bioWithNewlines = "Line 1\nLine 2\tTabbed";
        String result = ValidationUtils.validateAndCleanBio(bioWithNewlines);
        assertEquals(bioWithNewlines.trim(), result);
    }

    @Test
    void validatePassword_allSpecialCharacters_valid() {
        String[] specialChars = {
            "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_",
            "=", "+", "[", "]", "{", "}", ";", ":", "'", "\"", ",", ".",
            "<", ">", "/", "?", "\\", "|", "`", "~"
        };

        for (String specialChar : specialChars) {
            String password = "Password1" + specialChar;
            assertDoesNotThrow(() -> ValidationUtils.validatePassword(password),
                "Should accept special character: " + specialChar);
        }
    }

    @Test
    void validatePassword_boundaryLengths_valid() {
        String minLength = "Aa1!" + "a".repeat(4);
        assertDoesNotThrow(() -> ValidationUtils.validatePassword(minLength));

        String maxLength = "Aa1!" + "a".repeat(68);
        assertDoesNotThrow(() -> ValidationUtils.validatePassword(maxLength));
    }

    @Test
    void validatePassword_lengthBoundaries_throwsException() {
        String tooShort = "Aa1!abc";
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword(tooShort));

        String tooLong = "Aa1!" + "a".repeat(69);
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword(tooLong));
    }

    @Test
    void validatePassword_multipleOfEachRequirement_valid() {
        String password = "AABBCCaabbcc112233!@#$";
        assertDoesNotThrow(() -> ValidationUtils.validatePassword(password));
    }

    @Test
    void validatePassword_unicodeCharacters_mixed() {
        String passwordWithUnicode = "Password1!你好";
        assertDoesNotThrow(() -> ValidationUtils.validatePassword(passwordWithUnicode));
    }

    @Test
    void validatePassword_onlySpecialCharactersMissing_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("Password123"));
    }

    @Test
    void validatePassword_onlyUppercaseMissing_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("password123!"));
    }

    @Test
    void validatePassword_onlyLowercaseMissing_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("PASSWORD123!"));
    }

    @Test
    void validatePassword_onlyDigitMissing_throwsException() {
        assertThrows(InvalidPasswordException.class,
            () -> ValidationUtils.validatePassword("Password!"));
    }

    @Test
    void requireNotBlank_variousWhitespaceTypes_throwsException() {
        String[] whitespaceStrings = {
            "   ",
            "\t\t\t",
            "\n\n\n",
            "\r\r\r",
            " \t\n\r ",
            "\u00A0\u00A0\u00A0"
        };

        for (String whitespace : whitespaceStrings) {
            assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNotBlank(whitespace, "Test field"),
                "Should have failed for whitespace: " + whitespace.replace("\n", "\\n").replace("\t", "\\t"));
        }
    }

    @Test
    void requireNotBlank_validStrings_doesNotThrow() {
        String[] validStrings = {
            "a",
            "test",
            " a ",
            "\ta\t",
            "test string",
            "123",
            "!@#$%"
        };

        for (String valid : validStrings) {
            assertDoesNotThrow(() -> ValidationUtils.requireNotBlank(valid, "Test field"),
                "Should not have failed for: " + valid);
        }
    }

    @Test
    void validateAndCleanName_fieldNameInExceptionMessage() {
        try {
            ValidationUtils.validateAndCleanName(null, "Custom Field Name");
            fail("Expected exception was not thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Custom Field Name"));
        }
    }

    @Test
    void requireNotBlank_fieldNameInExceptionMessage() {
        try {
            ValidationUtils.requireNotBlank(null, "Another Field Name");
            fail("Expected exception was not thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Another Field Name"));
        }
    }

    @Test
    void validatePassword_detailedExceptionMessages() {
        try {
            ValidationUtils.validatePassword("short");
            fail("Expected exception was not thrown");
        } catch (InvalidPasswordException e) {
            assertTrue(e.getMessage().contains("8 characters"));
        }

        try {
            ValidationUtils.validatePassword("a".repeat(100));
            fail("Expected exception was not thrown");
        } catch (InvalidPasswordException e) {
            assertTrue(e.getMessage().contains("72 characters"));
        }
    }

    @Test
    void validateAndCleanEmail_casePreservation() {
        String mixedCaseEmail = "User.Name@DOMAIN.COM";
        String result = ValidationUtils.validateAndCleanEmail(mixedCaseEmail);
        assertEquals(mixedCaseEmail, result);
    }

    @Test
    void validateAndCleanUsername_casePreservation() {
        String mixedCaseUsername = "UserName123";
        String result = ValidationUtils.validateAndCleanUsername(mixedCaseUsername);
        assertEquals(mixedCaseUsername, result);
    }

    @Test
    void validateAndCleanName_casePreservation() {
        String mixedCaseName = "McDonald";
        String result = ValidationUtils.validateAndCleanName(mixedCaseName, "Last name");
        assertEquals(mixedCaseName, result);
    }

    @Test
    void validatePassword_complexValidPassword() {
        String complexPassword = "MyVeryComplexP@ssw0rd2024!";
        assertDoesNotThrow(() -> ValidationUtils.validatePassword(complexPassword));
    }

    @Test
    void validateAndCleanBio_htmlTags_valid() {
        String bioWithHtml = "<b>Hello</b> world & <i>more</i>";
        String result = ValidationUtils.validateAndCleanBio(bioWithHtml);
        assertEquals(bioWithHtml, result);
    }

}