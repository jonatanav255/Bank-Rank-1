package com.bankrank;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;

import com.bankrank.database.AccountDAO;
import com.bankrank.database.DatabaseConnection;
import com.bankrank.model.Account;
import com.bankrank.model.SavingsAccountType;
import com.bankrank.model.CheckingAccountType;

public class Main {

    public static void main(String[] args) {
        // Test database connection
        if (!DatabaseConnection.testConnection()) {
            System.out.println("Failed to connect to database!");
            return;
        }
        System.out.println("Database connected successfully!\n");
        SavingsAccountType savingsType = new SavingsAccountType();
        CheckingAccountType checkingType = new CheckingAccountType();

        Account alice = new Account(UUID.randomUUID(), "Alice", new BigDecimal("500.00"), savingsType);
        Account bob = new Account(UUID.randomUUID(), "Bob", new BigDecimal("200.00"), checkingType);

        // Perform transactions
        alice.deposit(new BigDecimal("100.00"));
        alice.withdraw(new BigDecimal("50.00"));
        alice.transferTo(bob, new BigDecimal("150.00"));
        bob.deposit(new BigDecimal("75.00"));
        bob.withdraw(new BigDecimal("25.00"));

        // Apply interest
        BigDecimal aliceInterest = alice.applyInterest();
        BigDecimal bobInterest = bob.applyInterest();

        System.out.println("=== Interest Applied ===");
        System.out.println("Alice (Savings 2.5%): $" + aliceInterest);
        System.out.println("Bob (Checking 0%): $" + bobInterest);
        System.out.println();

        // Show transaction histories
        System.out.println("=== Alice's Transaction History ===");
        alice.getTransactionHistory().forEach(t ->
            System.out.println(t.getTransactionType() + " - $" + t.getAmount() + " - " + t.getDescription())
        );

        System.out.println("\n=== Bob's Transaction History ===");
        bob.getTransactionHistory().forEach(t ->
            System.out.println(t.getTransactionType() + " - $" + t.getAmount() + " - " + t.getDescription())
        );

        System.out.println("\nFinal Balances:");
        System.out.println("Alice: $" + alice.getBalance());
        System.out.println("Bob: $" + bob.getBalance());

        // Test database persistence
        System.out.println("\n=== Testing Database ===");
        AccountDAO dao = new AccountDAO();

        try {
            // Save Alice's account
            dao.save(alice);
            System.out.println("Saved Alice to database");

            // Load Alice from database
            Account loadedAlice = dao.findById(alice.getAccountNumber());
            if (loadedAlice != null) {
                System.out.println("Loaded from database: " + loadedAlice.getCustomerName() +
                                 " - Balance: $" + loadedAlice.getBalance());
            }

            // Show all accounts
            System.out.println("\nAll accounts in database:");
            for (Account acc : dao.findAll()) {
                System.out.println(acc.getCustomerName() + " - $" + acc.getBalance());
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
