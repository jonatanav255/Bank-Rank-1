package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.model.Account;
import com.bankrank.model.Transaction;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Handles all reporting operations (transaction history, statements).
 */
public class ReportMenu {

    @SuppressWarnings("unused")
    private final Scanner scanner;
    private final AccountDAO accountDAO;
    private final InputHelper inputHelper;

    public ReportMenu(Scanner scanner, AccountDAO accountDAO) {
        this.scanner = scanner;
        this.accountDAO = accountDAO;
        this.inputHelper = new InputHelper(scanner);
    }

    public void viewTransactionHistory() {
        System.out.println("\n--- Transaction History ---");

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

            System.out.println("\nAccount: " + account.getCustomerName());
            System.out.println("Current Balance: $" + account.getBalance());
            System.out.println("\nTransactions:");

            List<Transaction> transactions = account.getTransactionHistory();

            if (transactions.isEmpty()) {
                System.out.println("No transactions found.");
                return;
            }

            System.out.println("╔════════════════╦═════════════╦══════════════════════════════════════╗");
            System.out.printf("║ %-14s ║ %-11s ║ %-36s ║%n", "Type", "Amount", "Description");
            System.out.println("╠════════════════╬═════════════╬══════════════════════════════════════╣");

            for (Transaction t : transactions) {
                System.out.printf("║ %-14s ║ $%-10s ║ %-36s ║%n",
                        t.getTransactionType(),
                        t.getAmount(),
                        truncate(t.getDescription(), 36));
            }

            System.out.println("╚════════════════╩═════════════╩══════════════════════════════════════╝");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void generateStatement() {
        System.out.println("\n--- Generate Statement ---");

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

            // TODO: Implement full statement generation with date ranges and file export
            System.out.println("\n✓ Statement generation coming soon!");
            System.out.println("Account: " + account.getCustomerName());
            System.out.println("Balance: $" + account.getBalance());

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
