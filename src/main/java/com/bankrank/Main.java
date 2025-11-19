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
            var results1 = transactionDAO.searchTransactions(null, null, null, null, null);
            System.out.println("Found " + results1.size() + " transactions");

            // Test with description
            System.out.println("\nTest 2: Description = 'deposit'");
            var results2 = transactionDAO.searchTransactions(null, "deposit", null, null, null);
            System.out.println("Found " + results2.size() + " transactions");
            results2.forEach(t -> System.out.println("  - " + t.getDescription() + ": $" + t.getAmount()));

            // Test with type
            System.out.println("\nTest 3: Type = DEPOSIT");
            var results3 = transactionDAO.searchTransactions(null, null, TransactionType.DEPOSIT, null, null);
            System.out.println("Found " + results3.size() + " transactions");

            // Test with amount range
            System.out.println("\nTest 4: Amount between 100 and 500");
            var results4 = transactionDAO.searchTransactions(null, null, null, new BigDecimal("100"), new BigDecimal("500"));
            System.out.println("Found " + results4.size() + " transactions");

            System.out.println("\n✓ All tests completed!");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
