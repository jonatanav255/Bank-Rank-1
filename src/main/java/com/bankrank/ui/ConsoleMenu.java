package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.database.DatabaseConnection;
import com.bankrank.model.Account;
import com.bankrank.model.AccountType;
import com.bankrank.model.CheckingAccountType;
import com.bankrank.model.SavingsAccountType;
import com.bankrank.model.Transaction;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Console-based user interface for the banking application.
 * Provides menu-driven interaction for all banking operations.
 */
public class ConsoleMenu {

    private final Scanner scanner;
    private final AccountDAO accountDAO;
    private boolean running;

    public ConsoleMenu() {
        this.scanner = new Scanner(System.in);
        this.accountDAO = new AccountDAO();
        this.running = true;
    }

    /**
     * Starts the console menu loop.
     */
    public void start() {
        // Test database connection
        if (!DatabaseConnection.testConnection()) {
            System.out.println("ERROR: Cannot connect to database!");
            System.out.println("Please check your database configuration in db.properties");
            return;
        }

        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║   Welcome to Bank Rank System!    ║");
        System.out.println("╚════════════════════════════════════╝\n");

        while (running) {
            showMainMenu();
            int choice = getIntInput("Enter choice: ");
            handleMainMenu(choice);
        }

        System.out.println("\nThank you for using Bank Rank!");
        scanner.close();
    }

    private void showMainMenu() {
        System.out.println("\n═══ MAIN MENU ═══");
        System.out.println("1. Create New Account");
        System.out.println("2. View All Accounts");
        System.out.println("3. Deposit Money");
        System.out.println("4. Withdraw Money");
        System.out.println("5. Transfer Money");
        System.out.println("6. View Transaction History");
        System.out.println("7. Apply Interest");
        System.out.println("8. Exit");
        System.out.println("═════════════════");
    }

    private void handleMainMenu(int choice) {
        switch (choice) {
            case 1 -> createAccount();
            case 2 -> viewAllAccounts();
            case 3 -> deposit();
            case 4 -> withdraw();
            case 5 -> transfer();
            case 6 -> viewTransactionHistory();
            case 7 -> applyInterest();
            case 8 -> exit();
            default -> System.out.println("Invalid choice. Please try again.");
        }
    }

    private void createAccount() {
        System.out.println("\n--- Create New Account ---");

        String customerName = getStringInput("Enter customer name: ");

        System.out.println("Select account type:");
        System.out.println("1. Savings (2.5% interest, $100 minimum balance)");
        System.out.println("2. Checking (0% interest, $0 minimum balance)");
        int typeChoice = getIntInput("Enter choice: ");

        AccountType accountType;
        if (typeChoice == 1) {
            accountType = new SavingsAccountType();
        } else if (typeChoice == 2) {
            accountType = new CheckingAccountType();
        } else {
            System.out.println("Invalid account type!");
            return;
        }

        BigDecimal initialDeposit = getBigDecimalInput("Enter initial deposit: $");

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

    private void viewAllAccounts() {
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

    private void deposit() {
        System.out.println("\n--- Deposit Money ---");

        UUID accountId = getAccountId();
        if (accountId == null) return;

        try {
            Account account = accountDAO.findById(accountId);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            BigDecimal amount = getBigDecimalInput("Enter deposit amount: $");

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

    private void withdraw() {
        System.out.println("\n--- Withdraw Money ---");

        UUID accountId = getAccountId();
        if (accountId == null) return;

        try {
            Account account = accountDAO.findById(accountId);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            BigDecimal amount = getBigDecimalInput("Enter withdrawal amount: $");

            boolean success = account.withdraw(amount);

            if (success) {
                accountDAO.update(account);
                System.out.println("\n✓ Withdrawal successful!");
                System.out.println("New balance: $" + account.getBalance());
            } else {
                System.out.println("\n✗ Withdrawal failed!");
                System.out.println("Insufficient funds or would violate minimum balance requirement.");
                System.out.println("Current balance: $" + account.getBalance());
                System.out.println("Minimum balance: $" + account.getAccountType().getMinimumBalance());
            }

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void transfer() {
        System.out.println("\n--- Transfer Money ---");

        System.out.println("Source account:");
        UUID sourceId = getAccountId();
        if (sourceId == null) return;

        System.out.println("Destination account:");
        UUID destId = getAccountId();
        if (destId == null) return;

        try {
            Account sourceAccount = accountDAO.findById(sourceId);
            Account destAccount = accountDAO.findById(destId);

            if (sourceAccount == null || destAccount == null) {
                System.out.println("One or both accounts not found!");
                return;
            }

            BigDecimal amount = getBigDecimalInput("Enter transfer amount: $");

            boolean success = sourceAccount.transferTo(destAccount, amount);

            if (success) {
                accountDAO.update(sourceAccount);
                accountDAO.update(destAccount);

                System.out.println("\n✓ Transfer successful!");
                System.out.println("From: " + sourceAccount.getCustomerName() + " - New balance: $" + sourceAccount.getBalance());
                System.out.println("To: " + destAccount.getCustomerName() + " - New balance: $" + destAccount.getBalance());
            } else {
                System.out.println("\n✗ Transfer failed!");
                System.out.println("Insufficient funds, same account, or would violate minimum balance.");
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    private void viewTransactionHistory() {
        System.out.println("\n--- Transaction History ---");

        UUID accountId = getAccountId();
        if (accountId == null) return;

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

    private void applyInterest() {
        System.out.println("\n--- Apply Interest ---");

        UUID accountId = getAccountId();
        if (accountId == null) return;

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

    private void exit() {
        System.out.println("\nExiting...");
        running = false;
    }

    // Helper methods for input

    private String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please try again.");
            }
        }
    }

    private BigDecimal getBigDecimalInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine().trim();
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please try again.");
            }
        }
    }

    private UUID getAccountId() {
        String input = getStringInput("Enter account ID: ");
        try {
            return UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid account ID format!");
            return null;
        }
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
