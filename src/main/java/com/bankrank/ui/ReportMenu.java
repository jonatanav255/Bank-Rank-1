package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.model.Account;
import com.bankrank.model.Transaction;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
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

            LocalDate startDate = inputHelper.getDateInput();
            LocalDate endDate = inputHelper.getDateInput();

            if (startDate != null && endDate != null) {
                if (startDate.compareTo(endDate) > 0) {
                    System.out.println("Error: Start date cannot be after end date");
                    return;
                }
            }

            System.out.println("Start: " + (startDate != null ? startDate : "all history"));
            System.out.println("End: " + (endDate != null ? endDate : "today"));

            List<Transaction> transactionsHistory = account.getTransactionHistory();
            List<Transaction> filteredListOfTransactions = new ArrayList<>();

            for (Transaction t : transactionsHistory) {

                LocalDate txnDate = t.getDateTime().toLocalDate();
                boolean beforeEnd = (endDate == null) || !txnDate.isAfter(endDate);
                boolean afterStart = (startDate == null) || !txnDate.isBefore(startDate);

                if (afterStart && beforeEnd) {
                    filteredListOfTransactions.add(t);
                }
            }

            // Print filtered transactions
            if (filteredListOfTransactions.isEmpty()) {
                System.out.println("\nNo transactions found in this date range.");
                return;
            }

            System.out.println("\nFiltered Transactions (" + filteredListOfTransactions.size() + " found):");
            System.out.println("╔════════════════╦═════════════╦══════════════════════════════════════╗");
            System.out.printf("║ %-14s ║ %-11s ║ %-36s ║%n", "Type", "Amount", "Description");
            System.out.println("╠════════════════╬═════════════╬══════════════════════════════════════╣");

            for (Transaction t : filteredListOfTransactions) {
                System.out.printf("║ %-14s ║ $%-10s ║ %-36s ║%n",
                        t.getTransactionType(),
                        t.getAmount(),
                        truncate(t.getDescription(), 36));
            }

            System.out.println("╚════════════════╩═════════════╩══════════════════════════════════════╝");

            // Ask user if they want to export
            System.out.println("\nExport options:");
            System.out.println("1. Screen only (done)");
            System.out.println("2. Export to CSV file");
            int exportChoice = inputHelper.getIntInput("Enter choice: ");

            if (exportChoice == 2) {
                exportToCSV(account, filteredListOfTransactions, startDate, endDate);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void exportToCSV(Account account, List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        System.out.println("\nGenerating CSV...");

        // Build CSV content
        StringBuilder csv = new StringBuilder();

        // Add header
        csv.append("Date,Type,Amount,Description\n");

        // Add each transaction
        for (Transaction t : transactions) {
            csv.append(t.getDateTime().toLocalDate()).append(",");
            csv.append(t.getTransactionType()).append(",");
            csv.append(t.getAmount()).append(",");
            // Escape commas in description by wrapping in quotes
            String description = t.getDescription();
            if (description.contains(",")) {
                csv.append("\"").append(description).append("\"");
            } else {
                csv.append(description);
            }
            csv.append("\n");
        }

        System.out.println("CSV content generated (" + transactions.size() + " transactions)");
        // TODO: Write to file
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
