package com.cab302.peerpractice.Model.Managers;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailService {

    private static final String emailFrom = "peerpracticecab302@gmail.com";
    private static final String appPassword = "wxvv yyps qbxf coin";

    // Flag to disable actual email sending during tests
    private static boolean testMode = false;

    public static void setTestMode(boolean testMode) {
        MailService.testMode = testMode;
    }

    public boolean sendMessage(String msg, String destination) {
        // Basic input validation
        if (msg == null || destination == null || destination.trim().isEmpty()) {
            return false;
        }

        destination = destination.trim();

        // Basic email format validation
        if (!isValidEmail(destination)) {
            return false;
        }

        try {
            // In test mode, just return true without sending actual email
            if (testMode) {
                return true;
            }

            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, false));
            message.setSubject("Password reset");
            message.setText(msg);
            Transport.send(message);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        // Basic email validation
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        email = email.trim();

        // Must contain @ and have parts before and after
        int atIndex = email.indexOf('@');
        if (atIndex <= 0 || atIndex >= email.length() - 1) {
            return false;
        }

        // No multiple @ symbols
        if (email.indexOf('@', atIndex + 1) != -1) {
            return false;
        }

        String domain = email.substring(atIndex + 1);

        // Special case: localhost is valid (common for testing)
        if ("localhost".equals(domain)) {
            return true;
        }

        // Reject international domain names (non-ASCII characters)
        if (!domain.matches("[a-zA-Z0-9.-]+")) {
            return false;
        }

        // Basic domain validation - must have at least one dot after @ (except localhost)
        return domain.contains(".") && !domain.endsWith(".") && !domain.startsWith(".");
    }

    private static jakarta.mail.Session getEmailSession(){
        return jakarta.mail.Session.getInstance(getGmailProperties(), new Authenticator(){
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(emailFrom,appPassword);
            }
        });
    }

    private static Properties getGmailProperties(){
        Properties prop = new Properties();
        prop.put("mail.smtp.auth","true");
        prop.put("mail.smtp.starttls.enable","true");
        prop.put("mail.smtp.host","smtp.gmail.com");
        prop.put("mail.smtp.port","587");
        prop.put("mail.smtp.ssl.trust","smtp.gmail.com");
        return prop;
    }

}
