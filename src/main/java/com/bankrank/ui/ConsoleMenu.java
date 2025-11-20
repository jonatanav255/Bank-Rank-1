package com.bankrank.ui;

import com.bankrank.database.AccountDAO;
import com.bankrank.database.DatabaseConnection;

import java.util.Scanner;

/**
 * Main console menu - delegates to specialized menu classes.
 */
public class ConsoleMenu {

    private final Scanner scanner;
    private final AccountDAO accountDAO;
    private final InputHelper inputHelper;
    private boolean running;

    // Specialized menu handlers
    private final AccountMenu accountMenu;
    private final TransactionMenu transactionMenu;
    private final ReportMenu reportMenu;

    public ConsoleMenu() {
        this.scanner = new Scanner(System.in);
        this.accountDAO = new AccountDAO();
        this.inputHelper = new InputHelper(scanner);
        this.running = true;

        // Initialize specialized menus
        this.accountMenu = new AccountMenu(scanner, accountDAO);
        this.transactionMenu = new TransactionMenu(scanner, accountDAO);
        this.reportMenu = new ReportMenu(scanner, accountDAO);
    }

    /**
     * Starts the console menu loop.
     */
    @SuppressWarnings("ConvertToTryWithResources")
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
            int choice = inputHelper.getIntInput("Enter choice: ");
            handleMainMenu(choice);
        }

        System.out.println("\nThank you for using Bank Rank!");
        scanner.close();
    }

    private void showMainMenu() {
        System.out.println("\n═══ MAIN MENU ═══");
        System.out.println("1. Create New Account");
        System.out.println("2. View All Accounts");
        System.out.println("3. View Account by ID");
        System.out.println("4. Deposit Money");
        System.out.println("5. Withdraw Money");
        System.out.println("6. Transfer Money");
        System.out.println("7. View Transaction History");
        System.out.println("8. Apply Interest");
        System.out.println("9. Generate Account Statement");
        System.out.println("10. Search/Filter Accounts");
        System.out.println("11. Search Transactions");
        System.out.println("12. Setup PIN for Existing Account");
        System.out.println("13. Change Customer Name");
        System.out.println("14. Change PIN");
        System.out.println("15. Exit");
        System.out.println("═════════════════");
    }

    private void handleMainMenu(int choice) {
        switch (choice) {
            case 1 ->
                accountMenu.createAccount();
            case 2 ->
                accountMenu.viewAllAccounts();
            case 3 ->
                accountMenu.viewAccountById();
            case 4 ->
                transactionMenu.deposit();
            case 5 ->
                transactionMenu.withdraw();
            case 6 ->
                transactionMenu.transfer();
            case 7 ->
                reportMenu.viewTransactionHistory();
            case 8 ->
                transactionMenu.applyInterest();
            case 9 ->
                reportMenu.generateStatement();
            case 10 ->
                accountMenu.searchAccounts();
            case 11 ->
                reportMenu.searchTransactions();
            case 12 ->
                accountMenu.setupPin();
            case 13 ->
                accountMenu.changeCustomerName();
            case 14 ->
                exit();
            default ->
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void exit() {
        System.out.println("\nExiting...");
        running = false;
    }
}
