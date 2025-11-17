package com.bankrank.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

public class EmailService {
//   1. Load email config from properties file

    private final Properties emailConfig;
    private final Session session;

    public EmailService() {
        this.emailConfig = loadEmailConfig();
        this.session = createSession();
    }

    private Properties loadEmailConfig() {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("email.properties")) {

            if (input == null) {
                System.out.println("unable to find email.properties");
            }

            props.load(input);

        } catch (IOException e) {
            System.out.println("Error loading email config: " + e.getMessage());
        }

        return props;
    }

    //   2. Create Session with SMTP settings + auth
    private Session createSession() {
        String username = emailConfig.getProperty("mail.user");
        String password = emailConfig.getProperty("mail.password");

        Session session = Session.getInstance(emailConfig, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        return session;
    }

    public void sendStatementEmail(String recipientEmail, String accountNumber, File csvFile) {

        // Validation
        if (session == null) {
            System.out.println("Email session not initialized");
            return;
        }

        if (csvFile == null || !csvFile.exists()) {
            System.out.println("CSV file not found");
            return;
        }

        try {
            MimeMessage message = new MimeMessage(session);

            message.setFrom(new InternetAddress(emailConfig.getProperty("mail.from")));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Bank Statement - Account " + accountNumber);

            Multipart multipart = new MimeMultipart();
            BodyPart textPart = new MimeBodyPart();
            MimeBodyPart filePart = new MimeBodyPart();

            textPart.setText(
                    """
                    Dear Customer,
                    
                    Please find your bank statement attached for account """ + accountNumber + ".\n\n"
                    + "Thank you for banking with us.\n\n"
                    + "Best regards,\n"
                    + "Bank Rank System"
            );
            multipart.addBodyPart(textPart);
            filePart.attachFile(csvFile);
            multipart.addBodyPart(filePart);
            message.setContent(multipart);

            Transport.send(message);
            System.out.println("✓ Email sent successfully to " + recipientEmail);

            // Body + attachment handled in next steps (Multipart)
        } catch (MessagingException | IOException e) {  // Catch both
            System.out.println(" Error sending email: " + e.getMessage());
            e.printStackTrace();
            // See full error for debugging
        }
    }

    // Test method
    public void testLoadConfig() {
        Properties props = emailConfig;

        if (props == null) {
            System.out.println("❌ Failed to load properties");
            return;
        }
        System.out.println("✓ Properties loaded successfully!");
        System.out.println("Host: " + props.getProperty("mail.smtp.host"));
        System.out.println("Port: " + props.getProperty("mail.smtp.port"));
        System.out.println("User: " + props.getProperty("mail.user"));
        System.out.println("From: " + props.getProperty("mail.from"));
    }

    public void testCreateSession() {
        if (session == null) {
            System.out.println("❌ Failed to create session");
            return;
        }

        System.out.println("✓ Session created successfully!");
        System.out.println("Session properties: " + session.getProperties());

    }

    public void testSendEmail() {
        File testFile = new File("statements/test_statement.csv");

        if (!testFile.exists()) {
            System.out.println("Test file not found. Create a CSV file first.");
            return;
        }

        sendStatementEmail(
                "jonatanav255second@gmail.com", // Your email to receive test
                "123456",
                testFile
        );
    }
}

//   3. Create MimeMessage
//   4. Create Multipart
//   5. Add text BodyPart
//   6. Add file BodyPart
//   7. Set multipart as message content
//   8. Transport.send(message)
