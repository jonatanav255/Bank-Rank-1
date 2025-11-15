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
            System.out.println("2. Export to Text file");
            System.out.println("3. Export to CSV file");
            System.out.println("4. Export to JSON file");
            int exportChoice = inputHelper.getIntInput("Enter choice: ");

            if (exportChoice == 2) {
                exportToText(account, filteredListOfTransactions, startDate, endDate);
            } else if (exportChoice == 3) {
                exportToCSV(account, filteredListOfTransactions, startDate, endDate);
            } else if (exportChoice == 4) {
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

    private void exportToText(Account account, List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {
        System.out.println("\nGenerating text statement...");

        StringBuilder text = new StringBuilder();

        // Header
        text.append("═══════════════════════════════════════════════════════════════════════════\n");
        text.append("                          BANK RANK STATEMENT                               \n");
        text.append("═══════════════════════════════════════════════════════════════════════════\n\n");

        // Account info
        text.append("Account ID:       ").append(account.getAccountNumber()).append("\n");
        text.append("Customer:         ").append(account.getCustomerName()).append("\n");
        text.append("Account Type:     ").append(account.getAccountType().getClass().getSimpleName().replace("AccountType", "")).append("\n");
        text.append("Current Balance:  $").append(account.getBalance()).append("\n");
        text.append("Statement Period: ").append(startDate != null ? startDate : "Beginning").append(" to ").append(endDate != null ? endDate : "Today").append("\n");
        text.append("Generated:        ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n\n");

        // Transactions
        text.append("───────────────────────────────────────────────────────────────────────────\n");
        text.append("                            TRANSACTIONS                                   \n");
        text.append("───────────────────────────────────────────────────────────────────────────\n\n");

        if (transactions.isEmpty()) {
            text.append("No transactions found in this period.\n\n");
        } else {
            for (Transaction t : transactions) {
                text.append(String.format("%-12s  %-15s  $%-12s\n",
                        t.getDateTime().toLocalDate(),
                        t.getTransactionType(),
                        t.getAmount()));
                text.append("  ").append(t.getDescription()).append("\n\n");
            }
        }

        // Summary
        text.append("───────────────────────────────────────────────────────────────────────────\n");
        text.append("                              SUMMARY                                      \n");
        text.append("───────────────────────────────────────────────────────────────────────────\n\n");

        java.math.BigDecimal totalDeposits = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalWithdrawals = java.math.BigDecimal.ZERO;
        java.math.BigDecimal totalInterest = java.math.BigDecimal.ZERO;

        for (Transaction t : transactions) {
            switch (t.getTransactionType()) {
                case DEPOSIT -> totalDeposits = totalDeposits.add(t.getAmount());
                case WITHDRAWAL -> totalWithdrawals = totalWithdrawals.add(t.getAmount());
                case INTEREST -> totalInterest = totalInterest.add(t.getAmount());
            }
        }

        text.append("Total Deposits:    $").append(totalDeposits).append("\n");
        text.append("Total Withdrawals: $").append(totalWithdrawals).append("\n");
        text.append("Total Interest:    $").append(totalInterest).append("\n");
        text.append("Transaction Count: ").append(transactions.size()).append("\n\n");

        text.append("═══════════════════════════════════════════════════════════════════════════\n");
        text.append("                       END OF STATEMENT                                    \n");
        text.append("═══════════════════════════════════════════════════════════════════════════\n");

        System.out.println("Text statement generated (" + transactions.size() + " transactions)");

        // Create statements directory
        File statementsDir = new File("statements");
        if (!statementsDir.exists()) {
            statementsDir.mkdir();
        }

        // Generate filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "statement_" + account.getAccountNumber() + "_" + timestamp + ".txt";
        File file = new File(statementsDir, filename);

        // Write to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text.toString());
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
