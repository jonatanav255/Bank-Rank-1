package com.bankrank;

import java.util.UUID;

import com.bankrank.model.Account;
import com.bankrank.model.SavingsAccountType;
import com.bankrank.model.CheckingAccountType;

public class Main {

    public static void main(String[] args) {
        SavingsAccountType savingsType = new SavingsAccountType();
        CheckingAccountType checkingType = new CheckingAccountType();

        Account alice = new Account(UUID.randomUUID(), "Alice", 500.0, savingsType);
        Account bob = new Account(UUID.randomUUID(), "Bob", 200.0, checkingType);

        // Perform transactions
        alice.deposit(100.0);
        alice.withdraw(50.0);
        alice.transferTo(bob, 150.0);
        bob.deposit(75.0);
        bob.withdraw(25.0);

        // Apply interest
        double aliceInterest = alice.applyInterest();
        double bobInterest = bob.applyInterest();

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
    }
}
