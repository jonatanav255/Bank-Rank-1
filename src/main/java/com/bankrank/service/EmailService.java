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

        } catch (IOException e) {
            System.out.println("Error loading email config: " + e.getMessage());
        }

        return props;
    }
}
//   2. Create Session with SMTP settings + auth
//   3. Create MimeMessage
//   4. Create Multipart
//   5. Add text BodyPart
//   6. Add file BodyPart
//   7. Set multipart as message content
//   8. Transport.send(message)
