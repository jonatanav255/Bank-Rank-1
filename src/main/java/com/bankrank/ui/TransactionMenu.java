package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.model.Account;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.UUID;

/**
 * Handles all transaction-related operations (deposit, withdraw, transfer, interest).
 */
public class TransactionMenu {

    @SuppressWarnings("unused")
    private final Scanner scanner;
    private final AccountDAO accountDAO;
    private final InputHelper inputHelper;

    public TransactionMenu(Scanner scanner, AccountDAO accountDAO) {
        this.scanner = scanner;
        this.accountDAO = accountDAO;
        this.inputHelper = new InputHelper(scanner);
    }

    public void deposit() {
        System.out.println("\n--- Deposit Money ---");

        UUID accountId = inputHelper.getAccountId();
        if (accountId == null) {
            return;
        }

        try {
            Account account = accountDAO.findById(accountId);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            BigDecimal amount = inputHelper.getBigDecimalInput("Enter deposit amount: $");

            account.deposit(amount);
            accountDAO.update(account);

            System.out.println("\n✓ Deposit successful!");
            System.out.println("New balance: $" + account.getBalance());

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void withdraw() {
        System.out.println("\n--- Withdraw Money ---");

        UUID accountId = inputHelper.getAccountId();
        if (accountId == null) {
            return;
        }

        try {
            Account account = accountDAO.findById(accountId);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            // Verify PIN before withdrawal
            String pin = inputHelper.getPinInput("Enter PIN to authorize withdrawal: ");
            if (!accountDAO.verifyPin(accountId, pin)) {
                System.out.println("\n✗ Invalid PIN! Withdrawal cancelled.");
                return;
            }

            BigDecimal amount = inputHelper.getBigDecimalInput("Enter withdrawal amount: $");

            account.withdraw(amount);
            accountDAO.update(account);
            System.out.println("\n✓ Withdrawal successful!");
            System.out.println("New balance: $" + account.getBalance());

        } catch (IllegalArgumentException e) {
            System.out.println("\n✗ Withdrawal failed!");
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void transfer() {
        System.out.println("\n--- Transfer Money ---");

        System.out.println("Source account:");
        UUID sourceId = inputHelper.getAccountId();
        if (sourceId == null) {
            return;
        }

        System.out.println("Destination account:");
        UUID destId = inputHelper.getAccountId();
        if (destId == null) {
            return;
        }

        try {
            Account sourceAccount = accountDAO.findById(sourceId);
            Account destAccount = accountDAO.findById(destId);

            if (sourceAccount == null || destAccount == null) {
                System.out.println("One or both accounts not found!");
                return;
            }

            // Verify PIN before transfer
            String pin = inputHelper.getPinInput("Enter PIN to authorize transfer: ");
            if (!accountDAO.verifyPin(sourceId, pin)) {
                System.out.println("\n✗ Invalid PIN! Transfer cancelled.");
                return;
            }

            BigDecimal amount = inputHelper.getBigDecimalInput("Enter transfer amount: $");

            sourceAccount.transferTo(destAccount, amount);
            accountDAO.update(sourceAccount);
            accountDAO.update(destAccount);

            System.out.println("\n✓ Transfer successful!");
            System.out.println("From: " + sourceAccount.getCustomerName() + " - New balance: $" + sourceAccount.getBalance());
            System.out.println("To: " + destAccount.getCustomerName() + " - New balance: $" + destAccount.getBalance());

        } catch (IllegalArgumentException e) {
            System.out.println("\n✗ Transfer failed!");
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void applyInterest() {
        System.out.println("\n--- Apply Interest ---");

        UUID accountId = inputHelper.getAccountId();
        if (accountId == null) {
            return;
        }

        try {
            Account account = accountDAO.findById(accountId);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            BigDecimal interest = account.applyInterest();
            accountDAO.update(account);

            System.out.println("\n✓ Interest applied!");
            System.out.println("Interest earned: $" + interest);
            System.out.println("New balance: $" + account.getBalance());

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
