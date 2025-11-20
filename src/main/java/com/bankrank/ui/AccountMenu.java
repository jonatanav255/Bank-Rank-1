package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.model.Account;
import com.bankrank.model.AccountType;
import com.bankrank.model.CheckingAccountType;
import com.bankrank.model.SavingsAccountType;
import com.bankrank.util.PasswordUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

/**
 * Handles all account-related operations (create, view, search).
 */
public class AccountMenu {

    @SuppressWarnings("unused")
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

        // Get and confirm PIN
        System.out.println("\nSet up a 4-digit PIN for this account:");
        String pin = inputHelper.getPinInput("Enter PIN: ");
        String confirmPin = inputHelper.getPinInput("Confirm PIN: ");

        if (!pin.equals(confirmPin)) {
            System.out.println("PINs do not match! Account creation cancelled.");
            return;
        }

        try {
            // Hash the PIN before storing
            String pinHash = PasswordUtil.hashPin(pin);
            Account account = new Account(UUID.randomUUID(), customerName, initialDeposit, accountType, pinHash);
            accountDAO.save(account);
            System.out.println("\n✓ Account created successfully!");
            System.out.println("Account ID: " + account.getAccountNumber());
            System.out.println("Customer: " + customerName);
            System.out.println("Balance: $" + account.getBalance());
            System.out.println("PIN: ****  (securely stored)");
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

    public void searchAccounts() {
        System.out.println("\n--- Search/Filter Accounts ---");
        System.out.println("Leave fields empty to skip filter\n");

        // Get customer name filter (optional)
        System.out.print("Enter customer name (or press Enter to skip): ");
        String customerName = inputHelper.getStringInput("").trim();
        if (customerName.isEmpty()) {
            customerName = null;
        }

        // Get account type filter (optional)
        System.out.println("\nAccount type options:");
        System.out.println("1. SAVINGS");
        System.out.println("2. CHECKING");
        System.out.print("Enter choice (or press Enter to skip): ");
        String typeInput = inputHelper.getStringInput("").trim();
        String accountType = null;
        if (!typeInput.isEmpty()) {
            accountType = switch (typeInput) {
                case "1" ->
                    "SAVINGS";
                case "2" ->
                    "CHECKING";
                default -> {
                    System.out.println("Invalid type, skipping filter.");
                    yield null;
                }
            };
        }

        try {
            List<Account> accounts = accountDAO.searchAccounts(customerName, accountType);

            if (accounts.isEmpty()) {
                System.out.println("\nNo accounts found matching your criteria.");
                return;
            }

            System.out.println("\nFound " + accounts.size() + " account(s):\n");
            System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
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
            System.out.println("Error searching accounts: " + e.getMessage());
        }
    }

    public void setupPin() {
        System.out.println("\n--- Setup PIN for Existing Account ---");

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

            // Check if account already has a PIN
            if (account.getPinHash() != null && !account.getPinHash().isEmpty()) {
                System.out.println("\n⚠ This account already has a PIN!");
                System.out.println("Use 'Change PIN' feature instead (if available).");
                return;
            }

            // Get and confirm new PIN
            System.out.println("\nSet up a 4-digit PIN for this account:");
            String pin = inputHelper.getPinInput("Enter PIN: ");
            String confirmPin = inputHelper.getPinInput("Confirm PIN: ");

            if (!pin.equals(confirmPin)) {
                System.out.println("PINs do not match! Setup cancelled.");
                return;
            }

            // Hash and save PIN
            String pinHash = PasswordUtil.hashPin(pin);
            account.setPinHash(pinHash);
            accountDAO.update(account);

            System.out.println("\n✓ PIN setup successful!");
            System.out.println("Account: " + account.getCustomerName());
            System.out.println("Account ID: " + account.getAccountNumber());
            System.out.println("PIN: ****  (securely stored)");

        } catch (SQLException e) {
            System.out.println("Error setting up PIN: " + e.getMessage());
        }
    }

    public void changeCustomerName() {
        System.out.println("\n--- Change Customer Name ---");

        UUID accountId = inputHelper.getAccountId();
        if (accountId == null) {
            return;
        }

        try {
            // 1. Load account FIRST
            Account account = accountDAO.findById(accountId);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            // 2. Check if has PIN
            if (account.getPinHash() == null || account.getPinHash().isEmpty()) {
                System.out.println("This account has no PIN! Please set up a PIN first (option 12).");
                return;
            }

            // 3. Verify PIN (only ONCE, no confirm)
            String pin = inputHelper.getPinInput("Enter PIN to authorize: ");
            if (!accountDAO.verifyPin(accountId, pin)) {
                System.out.println("\n✗ Invalid PIN!");
                return;
            }

            // 4. Show old name
            String oldName = account.getCustomerName();
            System.out.println("\nCurrent name: " + oldName);

            // 5. Get new name
            String newName = inputHelper.getStringInput("Enter new name: ");

            // 6. Update (validation happens in setter)
            account.setCustomerName(newName);
            accountDAO.update(account);

            // 7. Confirm
            System.out.println("\n✓ Name changed successfully!");
            System.out.println("Old name: " + oldName);
            System.out.println("New name: " + account.getCustomerName());

        } catch (IllegalArgumentException e) {
            System.out.println("\n✗ Invalid name: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public void changePin() {
        System.out.println("\n--- Change PIN ---");

        UUID accountId = inputHelper.getAccountId();
        if (accountId == null) {
            return;
        }

        try {
            // 1. Load account
            Account account = accountDAO.findById(accountId);
            if (account == null) {
                System.out.println("Account not found!");
                return;
            }

            // 2. Check if has PIN
            if (account.getPinHash() == null || account.getPinHash().isEmpty()) {
                System.out.println("This account has no PIN! Please set up a PIN first (option 12).");
                return;
            }

            // 3. Verify old PIN
            String oldPin = inputHelper.getPinInput("Enter current PIN: ");
            if (!accountDAO.verifyPin(accountId, oldPin)) {
                System.out.println("\n✗ Invalid PIN!");
                return;
            }

            // 4. Get new PIN with confirmation
            System.out.println("\nEnter new PIN:");
            String newPin = inputHelper.getPinInput("New PIN: ");
            String confirmPin = inputHelper.getPinInput("Confirm new PIN: ");

            if (!newPin.equals(confirmPin)) {
                System.out.println("PINs do not match! Change cancelled.");
                return;
            }

            // 5. Check if new PIN is same as old
            if (PasswordUtil.verifyPin(newPin, account.getPinHash())) {
                System.out.println("\n✗ New PIN must be different from current PIN!");
                return;
            }

            // 6. Hash and save new PIN
            String newPinHash = PasswordUtil.hashPin(newPin);
            accountDAO.changePin(accountId, newPinHash);

            System.out.println("\n✓ PIN changed successfully!");
            System.out.println("Account: " + account.getCustomerName());
            System.out.println("Account ID: " + account.getAccountNumber());

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
