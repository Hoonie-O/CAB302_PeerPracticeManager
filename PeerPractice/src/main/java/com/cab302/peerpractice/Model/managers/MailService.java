package com.cab302.peerpractice.Model.managers;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailService {

    private static final String emailFrom = "peerpracticecab302@gmail.com";
    private static final String appPassword = "wxvv yyps qbxf coin";

    public boolean sendMessage(String msg, String destination) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(emailFrom));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(destination, false));
            message.setSubject("Password reset");
            message.setText(msg);
            Transport.send(message);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
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
