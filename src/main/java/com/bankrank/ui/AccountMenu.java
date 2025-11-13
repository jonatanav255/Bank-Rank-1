package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.model.Account;
import com.bankrank.model.AccountType;
import com.bankrank.model.CheckingAccountType;
import com.bankrank.model.SavingsAccountType;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Handles all account-related operations (create, view, search).
 */
public class AccountMenu {

    private final Scanner scanner;
    private final AccountDAO accountDAO;
    private final InputHelper inputHelper;

    public AccountMenu(Scanner scanner, AccountDAO accountDAO) {
        this.scanner = scanner;
        this.accountDAO = accountDAO;
        this.inputHelper = new InputHelper(scanner);
    }

    public void createAccount() {
        System.out.println("\n--- Create New Account ---");

        String customerName = inputHelper.getStringInput("Enter customer name: ");

        System.out.println("Select account type:");
        System.out.println("1. Savings (2.5% interest, $100 minimum balance)");
        System.out.println("2. Checking (0% interest, $0 minimum balance)");
        int typeChoice = inputHelper.getIntInput("Enter choice: ");

        AccountType accountType = switch (typeChoice) {
            case 1 ->
                new SavingsAccountType();
            case 2 ->
                new CheckingAccountType();
            default -> {
                System.out.println("Invalid account type!");
                yield null;
            }
        };

        if (accountType == null) {
            return;
        }

        BigDecimal initialDeposit = inputHelper.getBigDecimalInput("Enter initial deposit: $");

        if (initialDeposit.compareTo(BigDecimal.ZERO) < 0) {
            System.out.println("Initial deposit cannot be negative!");
            return;
        }

        // Check minimum balance requirement
        if (initialDeposit.compareTo(accountType.getMinimumBalance()) < 0) {
            System.out.println("Initial deposit must be at least $" + accountType.getMinimumBalance());
            return;
        }

        try {
            Account account = new Account(UUID.randomUUID(), customerName, initialDeposit, accountType);
            accountDAO.save(account);
            System.out.println("\n✓ Account created successfully!");
            System.out.println("Account ID: " + account.getAccountNumber());
            System.out.println("Customer: " + customerName);
            System.out.println("Balance: $" + account.getBalance());
        } catch (SQLException e) {
            System.out.println("Error creating account: " + e.getMessage());
        }
    }

    public void viewAllAccounts() {
        System.out.println("\n--- All Accounts ---");

        try {
            List<Account> accounts = accountDAO.findAll();

            if (accounts.isEmpty()) {
                System.out.println("No accounts found.");
                return;
            }

            System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
            System.out.printf("║ %-36s │ %-15s │ %-12s ║%n", "Account ID", "Customer", "Balance");
            System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

            for (Account account : accounts) {
                System.out.printf("║ %-36s │ %-15s │ $%-11s ║%n",
                        account.getAccountNumber().toString().substring(0, 36),
                        truncate(account.getCustomerName(), 15),
                        account.getBalance());
            }

            System.out.println("╚════════════════════════════════════════════════════════════════════════╝");

        } catch (SQLException e) {
            System.out.println("Error loading accounts: " + e.getMessage());
        }
    }

    public void viewAccountById() {
        System.out.println("\n--- View Account by ID ---");
        UUID accountId = inputHelper.getAccountId();
        if (accountId == null) {
            return;
        }

        try {
            Account account = accountDAO.findById(accountId);
            if (account != null) {
                int width = 70;
                String border = "═".repeat(width);

                System.out.println("\n╔" + border + "╗");
                System.out.println(String.format("║ Account ID: %-" + (width - 14) + "s ║", account.getAccountNumber()));
                System.out.println(String.format("║ Customer: %-" + (width - 12) + "s ║", account.getCustomerName()));
                System.out.println(String.format("║ Type: %-" + (width - 8) + "s ║", getAccountTypeName(account.getAccountType())));
                System.out.println(String.format("║ Balance: $%-" + (width - 12) + "s ║", account.getBalance()));
                System.out.println(String.format("║ Created: %-" + (width - 11) + "s ║", account.getDateCreated()));
                System.out.println("╚" + border + "╝");
            } else {
                System.out.println("Account not found!");
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

    private String getAccountTypeName(AccountType accountType) {
        if (accountType instanceof SavingsAccountType) {
            return "SAVINGS";
        } else if (accountType instanceof CheckingAccountType) {
            return "CHECKING";
        }
        throw new IllegalArgumentException("Unknown account type");
    }
}
