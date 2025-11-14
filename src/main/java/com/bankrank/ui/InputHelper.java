package com.bankrank.ui;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.UUID;

/**
 * Helper class for handling user input with validation.
 */
public class InputHelper {

    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    public BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please try again.");
            }
        }
    }

    public UUID getAccountId() {
        String input = getStringInput("Enter account ID: ");
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid account ID format!");
            return null;
        }
    }

    public LocalDate getDateInput() {
        while (true) {
            System.out.print("Enter date (YYYY-MM-DD) or press Enter to skip: ");
            String input = scanner.nextLine().trim();

            // Allow user to skip
            if (input.isEmpty()) {
                return null;
            }

            // Try to parse the date
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD (e.g., 2024-03-15)");
                // Loop continues
            }
        }
    }
}
