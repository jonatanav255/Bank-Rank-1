package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.model.Account;
import com.bankrank.model.Transaction;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            System.out.println("3. Export to JSON file");
            int exportChoice = inputHelper.getIntInput("Enter choice: ");

            if (exportChoice == 2) {
                exportToCSV(account, filteredListOfTransactions, startDate, endDate);
            } else if (exportChoice == 3) {
                exportToJSON(account, filteredListOfTransactions, startDate, endDate);
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

        // Create statements directory if it doesn't exist
        File statementsDir = new File("statements");
        if (!statementsDir.exists()) {
            statementsDir.mkdir();
        }

        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "statement_" + account.getAccountNumber() + "_" + timestamp + ".csv";
        File file = new File(statementsDir, filename);

        // Write CSV to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(csv.toString());
            System.out.println("\n✓ Statement exported successfully!");
            System.out.println("File saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("✗ Error writing file: " + e.getMessage());
        }
    }

    private void exportToJSON(Account account, List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        System.out.println("\nGenerating JSON...");

        // Build JSON content manually (no library needed)
        StringBuilder json = new StringBuilder();

        json.append("{\n");
        json.append("  \"accountId\": \"").append(account.getAccountNumber()).append("\",\n");
        json.append("  \"customerName\": \"").append(account.getCustomerName()).append("\",\n");
        json.append("  \"balance\": ").append(account.getBalance()).append(",\n");
        json.append("  \"startDate\": \"").append(startDate != null ? startDate : "all history").append("\",\n");
        json.append("  \"endDate\": \"").append(endDate != null ? endDate : "today").append("\",\n");
        json.append("  \"transactionCount\": ").append(transactions.size()).append(",\n");
        json.append("  \"transactions\": [\n");

        // Add each transaction
        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            json.append("    {\n");
            json.append("      \"date\": \"").append(t.getDateTime().toLocalDate()).append("\",\n");
            json.append("      \"type\": \"").append(t.getTransactionType()).append("\",\n");
            json.append("      \"amount\": ").append(t.getAmount()).append(",\n");
            json.append("      \"description\": \"").append(escapeJSON(t.getDescription())).append("\"\n");
            json.append("    }");
            if (i < transactions.size() - 1) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  ]\n");
        json.append("}\n");

        System.out.println("JSON content generated (" + transactions.size() + " transactions)");

        // Create statements directory if it doesn't exist
        File statementsDir = new File("statements");
        if (!statementsDir.exists()) {
            statementsDir.mkdir();
        }

        // Generate filename with timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "statement_" + account.getAccountNumber() + "_" + timestamp + ".json";
        File file = new File(statementsDir, filename);

        // Write JSON to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(json.toString());
            System.out.println("\n✓ Statement exported successfully!");
            System.out.println("File saved to: " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("✗ Error writing file: " + e.getMessage());
        }
    }

    private String escapeJSON(String str) {
        // Escape quotes and backslashes for JSON
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
