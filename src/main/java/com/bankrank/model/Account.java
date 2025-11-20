package com.bankrank.model;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Create new accounts with unique account numbers, customer name, and initial deposit
// Support two account types: Savings and Checking
// Each account tracks balance, account holder info, and creation date
// Implement proper encapsulation - balance shouldn't be directly accessible
public class Account {

    private final UUID accountNumber;
    private String customerName;
    private BigDecimal balance;
    private final LocalDate dateCreated;
    private final AccountType accountType;
    private final List<Transaction> transactionHistory;
    private String pinHash;
    private boolean isLocked;

    //constructor for new accounts (with PIN)
    public Account(UUID accountNumber, String customerName, BigDecimal initialValue, AccountType accountType, String pinHash) {
        this.accountNumber = accountNumber;
        setCustomerName(customerName); // Use setter for validation
        this.balance = initialValue;
        this.dateCreated = LocalDate.now();
        this.accountType = accountType;
        this.transactionHistory = new ArrayList<>();
        this.pinHash = pinHash;
        this.isLocked = false;
    }

    //constructor for loading from database (with all fields)
    public Account(UUID accountNumber, String customerName, BigDecimal initialValue, AccountType accountType, LocalDate dateCreated, List<Transaction> transactions, String pinHash, boolean isLocked) {
        this.accountNumber = accountNumber;
        setCustomerName(customerName); // Use setter for validation
        this.balance = initialValue;
        this.dateCreated = dateCreated;
        this.accountType = accountType;
        this.transactionHistory = new ArrayList<>(transactions);
        this.pinHash = pinHash;
        this.isLocked = isLocked;
    }

    // getters
    public UUID getAccountNumber() {
        return accountNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getPinHash() {
        return pinHash;
    }

    public final void setCustomerName(String newCustomerName) {
        // Validation 1: Cannot be null or empty
        if (newCustomerName == null || newCustomerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name cannot be empty");
        }

        // Validation 2: Length check (reasonable limit)
        if (newCustomerName.trim().length() > 100) {
            throw new IllegalArgumentException("Customer name too long (max 100 characters)");
        }

        // Validation 3: Only letters, spaces, hyphens, apostrophes
        if (!newCustomerName.matches("^[a-zA-Z\\s'-]+$")) {
            throw new IllegalArgumentException("Customer name can only contain letters, spaces, hyphens, and apostrophes");
        }

        this.customerName = newCustomerName.trim();
    }

    public void setPinHash(String pinHash) {
        this.pinHash = pinHash;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be 0 or a negative amount");
        }

        this.balance = balance.add(amount);
        Transaction depositTransaction = new Transaction(TransactionType.DEPOSIT, amount, "Deposit of $" + amount, LocalDateTime.now());
        transactionHistory.add(depositTransaction);
    }

    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be 0 or a negative amount");
        }

        if (!accountType.canWithdraw(balance, amount)) {
            throw new IllegalArgumentException("Insufficient funds or withdrawal would violate minimum balance requirement");
        }
        balance = balance.subtract(amount);
        Transaction withdraw = new Transaction(TransactionType.WITHDRAWAL, amount, "Withdraw $" + amount, LocalDateTime.now());
        transactionHistory.add(withdraw);
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public void transferTo(Account destinationAccount, BigDecimal amount) {

        // Validation 1: Amount must be positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        // Validation 2: Destination account must exist and be different
        if (destinationAccount == null) {
            throw new IllegalArgumentException("Destination account cannot be null");
        }

        // Validation 3: Can't transfer to the same account
        if (this.accountNumber.equals(destinationAccount.accountNumber)) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }

        // Validation 4: Source account must be able to withdraw this amount
        if (!this.accountType.canWithdraw(this.balance, amount)) {
            throw new IllegalArgumentException("Insufficient funds or transfer would violate minimum balance requirement");
        }

        // PHASE 2: EXECUTE (all validations passed, safe to proceed)
        this.balance = this.balance.subtract(amount);
        destinationAccount.balance = destinationAccount.balance.add(amount);

        Transaction sourceTransaction = new Transaction(TransactionType.TRANSFER, amount, "transfer to " + destinationAccount.customerName, LocalDateTime.now());
        Transaction destinationAccountTransaction = new Transaction(TransactionType.TRANSFER, amount, "Transfer from " + customerName, LocalDateTime.now());

        transactionHistory.add(sourceTransaction);
        destinationAccount.transactionHistory.add(destinationAccountTransaction);
    }

    public BigDecimal applyInterest() {

        BigDecimal interest = balance.multiply(accountType.getInterestRate());
        balance = balance.add(interest);

        Transaction applyInterestTransaction = new Transaction(TransactionType.INTEREST, interest, "Interest amount " + interest, LocalDateTime.now());
        transactionHistory.add(applyInterestTransaction);

        return interest;

    }
}
