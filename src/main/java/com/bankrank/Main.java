package com.bankrank;

import com.bankrank.service.EmailService;
import com.bankrank.ui.ConsoleMenu;

public class Main {

    public static void main(String[] args) {
        // ConsoleMenu menu = new ConsoleMenu();
        // menu.start();

        EmailService emailService = new EmailService();
        // emailService.testLoadConfig();
        // emailService.testCreateSession();
        emailService.testSendEmail();
    }
}
