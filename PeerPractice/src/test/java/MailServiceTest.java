import com.cab302.peerpractice.Model.Managers.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MailServiceTest {

    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailService = new MailService();
    }

    @Test
    void testSendMessageWithValidEmail() {
        String validEmail = "test@example.com";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithInvalidEmail() {
        String invalidEmail = "not-an-email";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, invalidEmail);

        assertFalse(result);
    }

    @Test
    void testSendMessageWithNullEmail() {
        String message = "Test message";

        boolean result = mailService.sendMessage(message, null);

        assertFalse(result);
    }

    @Test
    void testSendMessageWithEmptyEmail() {
        String emptyEmail = "";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, emptyEmail);

        assertFalse(result);
    }

    @Test
    void testSendMessageWithNullMessage() {
        String validEmail = "test@example.com";

        boolean result = mailService.sendMessage(null, validEmail);

        assertFalse(result);
    }

    @Test
    void testSendMessageWithEmptyMessage() {
        String validEmail = "test@example.com";
        String emptyMessage = "";

        boolean result = mailService.sendMessage(emptyMessage, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithLongMessage() {
        String validEmail = "test@example.com";
        String longMessage = "This is a very long message. ".repeat(100);

        boolean result = mailService.sendMessage(longMessage, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithSpecialCharacters() {
        String validEmail = "test@example.com";
        String specialMessage = "Message with special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?~`";

        boolean result = mailService.sendMessage(specialMessage, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithUnicodeCharacters() {
        String validEmail = "test@example.com";
        String unicodeMessage = "Unicode message: 你好世界 🌍 Привет мир نمونه متن";

        boolean result = mailService.sendMessage(unicodeMessage, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithNewlines() {
        String validEmail = "test@example.com";
        String messageWithNewlines = "Line 1\nLine 2\nLine 3";

        boolean result = mailService.sendMessage(messageWithNewlines, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithHtmlContent() {
        String validEmail = "test@example.com";
        String htmlMessage = "<html><body><h1>Test</h1><p>HTML content</p></body></html>";

        boolean result = mailService.sendMessage(htmlMessage, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageMultipleValidEmails() {
        String[] validEmails = {
            "test@example.com",
            "user.name@domain.co.uk",
            "test123@subdomain.example.org"
        };
        String message = "Test message";

        for (String email : validEmails) {
            boolean result = mailService.sendMessage(message, email);
            assertTrue(result);
        }
    }

    @Test
    void testSendMessageMultipleInvalidEmails() {
        String[] invalidEmails = {
            "notanemail",
            "@domain.com",
            "user@",
            "user@@domain.com",
            "user.domain.com"
        };
        String message = "Test message";

        for (String email : invalidEmails) {
            boolean result = mailService.sendMessage(message, email);
            assertFalse(result);
        }
    }

    @Test
    void testSendMessageWithWhitespaceEmail() {
        String[] whitespaceEmails = {
            "   ",
            "\t\t\t",
            "\n\n\n",
            " test@example.com ",
            "\ttest@example.com\t"
        };
        String message = "Test message";

        for (String email : whitespaceEmails) {
            boolean result = mailService.sendMessage(message, email);
            if (email.trim().contains("@")) {
                assertTrue(result);
            } else {
                assertFalse(result);
            }
        }
    }

    @Test
    void testSendMessageConsecutiveCalls() {
        String validEmail = "test@example.com";
        String message = "Consecutive test message";

        boolean result1 = mailService.sendMessage(message, validEmail);
        boolean result2 = mailService.sendMessage(message, validEmail);
        boolean result3 = mailService.sendMessage(message, validEmail);

        assertTrue((result1 && result2 && result3));
    }

    @Test
    void testSendMessageWithVeryLongEmail() {
        String longEmail = "a".repeat(100) + "@" + "domain".repeat(10) + ".com";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, longEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithInternationalDomain() {
        String internationalEmail = "test@münchen.de";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, internationalEmail);

        assertFalse(result);
    }

    @Test
    void testSendMessageWithNumericDomain() {
        String numericEmail = "test@123.456.789.012";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, numericEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithSubdomains() {
        String subdomainEmail = "test@mail.subdomain.example.com";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, subdomainEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithPlusAddressing() {
        String plusEmail = "test+tag@example.com";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, plusEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithDashInDomain() {
        String dashEmail = "test@my-domain.com";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, dashEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageWithNumbersInEmail() {
        String numericEmail = "user123@example123.com";
        String message = "Test message";

        boolean result = mailService.sendMessage(message, numericEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessageEdgeCaseEmails() {
        String[] edgeCaseEmails = {
            "a@b.co",
            "very.long.username@very.long.domain.com",
            "user.name+tag+sorting@example.com",
            "x@example.com",
            "test@localhost"
        };
        String message = "Edge case test";

        for (String email : edgeCaseEmails) {
            boolean result = mailService.sendMessage(message, email);
            assertTrue(result);
        }
    }

    @Test
    void testSendMessageWithTabsAndSpacesInMessage() {
        String validEmail = "test@example.com";
        String messageWithWhitespace = "Message\twith\ttabs\nand\nlines\rand\rcarriage\freturns";

        boolean result = mailService.sendMessage(messageWithWhitespace, validEmail);

        assertTrue(result);
    }

    @Test
    void testSendMessagePerformance() {
        String validEmail = "test@example.com";
        String message = "Performance test message";

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 5; i++) {
            mailService.sendMessage(message + " " + i, validEmail);
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 30000);
    }

    @Test
    void testSendMessageMemoryUsage() {
        String validEmail = "test@example.com";

        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        for (int i = 0; i < 10; i++) {
            String message = "Memory test message " + i;
            mailService.sendMessage(message, validEmail);
        }

        System.gc();
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;

        assertTrue(memoryIncrease < 10 * 1024 * 1024);
    }

    @Test
    void testMailServiceInstantiation() {
        MailService newMailService = new MailService();
        assertNotNull(newMailService);

        String validEmail = "test@example.com";
        String message = "Instantiation test";

        boolean result = newMailService.sendMessage(message, validEmail);
        assertTrue(result );
    }

    @Test
    void testMultipleMailServiceInstances() {
        MailService service1 = new MailService();
        MailService service2 = new MailService();
        MailService service3 = new MailService();

        String validEmail = "test@example.com";
        String message = "Multiple instances test";

        boolean result1 = service1.sendMessage(message, validEmail);
        boolean result2 = service2.sendMessage(message, validEmail);
        boolean result3 = service3.sendMessage(message, validEmail);

        assertTrue(result1 || result2 || result3);
    }
}