package com.bankrank.ui;

import com.bankrank.auth.AuthenticationService;
import com.bankrank.model.User;

import java.sql.SQLException;
import java.util.Scanner;

/**
 * Handles user login and authentication.
 */
public class LoginMenu {
    private final Scanner scanner;
    private final AuthenticationService authService;
    private final InputHelper inputHelper;

    public LoginMenu(Scanner scanner, AuthenticationService authService) {
        this.scanner = scanner;
        this.authService = authService;
        this.inputHelper = new InputHelper(scanner);
    }

    /**
     * Display login screen and authenticate user.
     *
     * @return true if login successful, false if user wants to exit
     */
    public boolean showLogin() {
        System.out.println("\n╔════════════════════════════════════╗");
        System.out.println("║          BANK RANK LOGIN          ║");
        System.out.println("╚════════════════════════════════════╝\n");

        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            System.out.print("Username (or 'exit' to quit): ");
            String username = scanner.nextLine().trim();

            if (username.equalsIgnoreCase("exit")) {
                return false;
            }

            if (username.isEmpty()) {
                System.out.println("Username cannot be empty!\n");
                continue;
            }

            System.out.print("Password: ");
            String password = scanner.nextLine();

            try {
                if (authService.login(username, password)) {
                    User user = authService.getCurrentUser();
                    System.out.println("\n✓ Login successful!");
                    System.out.println("Welcome, " + user.getFullName() + " (" + user.getRole() + ")");
                    return true;
                } else {
                    attempts++;
                    int remaining = MAX_ATTEMPTS - attempts;
                    if (remaining > 0) {
                        System.out.println("\n✗ Invalid username or password.");
                        System.out.println("Attempts remaining: " + remaining + "\n");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Database error: " + e.getMessage());
                attempts++;
            }
        }

        System.out.println("\n✗ Maximum login attempts exceeded. Exiting...");
        return false;
    }

    /**
     * Confirm logout.
     */
    public boolean confirmLogout() {
        System.out.print("\nAre you sure you want to logout? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }
}
