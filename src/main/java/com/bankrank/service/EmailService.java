package com.bankrank.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EmailService {
//   1. Load email config from properties file

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

    // Test method
    public void testLoadConfig() {
        Properties props = loadEmailConfig();

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
}
//   2. Create Session with SMTP settings + auth
//   3. Create MimeMessage
//   4. Create Multipart
//   5. Add text BodyPart
//   6. Add file BodyPart
//   7. Set multipart as message content
//   8. Transport.send(message)
