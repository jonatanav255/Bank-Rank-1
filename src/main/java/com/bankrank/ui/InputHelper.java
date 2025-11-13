package com.bankrank.ui;

import java.math.BigDecimal;
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
}
