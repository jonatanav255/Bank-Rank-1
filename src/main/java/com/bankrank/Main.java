package com.bankrank;

import java.util.UUID;

import com.bankrank.model.Account;
import com.bankrank.model.SavingsAccountType;
import com.bankrank.model.CheckingAccountType;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Welcome to Bank Rank! ===\n");

        // Create two accounts
        SavingsAccountType savingsType = new SavingsAccountType();
        CheckingAccountType checkingType = new CheckingAccountType();

        Account savingsAccount = new Account(UUID.randomUUID(), "Alice", 500.0, savingsType);
        Account checkingAccount = new Account(UUID.randomUUID(), "Bob", 200.0, checkingType);

        System.out.println("Initial Balances:");
        System.out.println("Alice (Savings): $" + savingsAccount.getBalance());
        System.out.println("Bob (Checking): $" + checkingAccount.getBalance());
        System.out.println();

        // Test 1: Valid transfer
        System.out.println("Test 1: Transfer $100 from Alice to Bob");
        boolean success = savingsAccount.transferTo(checkingAccount, 100.0);
        System.out.println("Transfer successful: " + success);
        System.out.println("Alice balance: $" + savingsAccount.getBalance());
        System.out.println("Bob balance: $" + checkingAccount.getBalance());
        System.out.println();

        // Test 2: Transfer that would violate minimum balance (Savings needs $100 minimum)
        System.out.println("Test 2: Try to transfer $350 from Alice (would go below $100 minimum)");
        success = savingsAccount.transferTo(checkingAccount, 350.0);
        System.out.println("Transfer successful: " + success);
        System.out.println("Alice balance: $" + savingsAccount.getBalance() + " (unchanged - atomic!)");
        System.out.println("Bob balance: $" + checkingAccount.getBalance() + " (unchanged - atomic!)");
        System.out.println();

        // Test 3: Deposit
        System.out.println("Test 3: Alice deposits $200");
        savingsAccount.deposit(200.0);
        System.out.println("Alice balance: $" + savingsAccount.getBalance());
        System.out.println();

        // Test 4: Withdraw
        System.out.println("Test 4: Bob withdraws $50");
        success = checkingAccount.withdraw(50.0);
        System.out.println("Withdrawal successful: " + success);
        System.out.println("Bob balance: $" + checkingAccount.getBalance());
    }
}
