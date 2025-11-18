package com.bankrank;

import com.bankrank.database.TransactionDAO;
import com.bankrank.model.TransactionType;
import com.bankrank.service.EmailService;
import com.bankrank.ui.ConsoleMenu;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        // TEST Transaction Search
        testTransactionSearch();

        // ConsoleMenu menu = new ConsoleMenu();
        // menu.start();

        // EmailService emailService = new EmailService();
        // emailService.testLoadConfig();
        // emailService.testCreateSession();
        // emailService.testSendEmail();
    }

    private static void testTransactionSearch() {
        System.out.println("=== TESTING TRANSACTION SEARCH ===\n");

        TransactionDAO transactionDAO = new TransactionDAO();

        try {
            // Test with no filters
            System.out.println("Test 1: No filters");
            transactionDAO.searchTransactions(null, null, null, null, null);

            // Test with description
            System.out.println("\nTest 2: Description = 'deposit'");
            transactionDAO.searchTransactions(null, "deposit", null, null, null);

            // Test with type
            System.out.println("\nTest 3: Type = DEPOSIT");
            transactionDAO.searchTransactions(null, null, TransactionType.DEPOSIT, null, null);

            // Test with amount range
            System.out.println("\nTest 4: Amount between 100 and 500");
            transactionDAO.searchTransactions(null, null, null, new BigDecimal("100"), new BigDecimal("500"));

            System.out.println("\n✓ All tests completed!");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
