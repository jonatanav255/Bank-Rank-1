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
                System.out.println(filteredListOfTransactions);

            }

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
