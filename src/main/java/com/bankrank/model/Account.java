package com.bankrank.model;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Create new accounts with unique account numbers, customer name, and initial deposit
// Support two account types: Savings and Checking
// Each account tracks balance, account holder info, and creation date
// Implement proper encapsulation - balance shouldn't be directly accessible
public class Account {

    private UUID AccountNumber;
    private String CustomerName;
    private BigDecimal Balance;
    private LocalDate DateCreated;
    private AccountType AccountType;
    private List<Transaction> transactionHistory;

    //constructor
    public Account(UUID AccountNumber, String CustomerName, BigDecimal InitialValue, AccountType AccountType) {
        this.AccountNumber = AccountNumber;
        this.CustomerName = CustomerName;
        this.Balance = InitialValue;
        this.DateCreated = LocalDate.now();
        this.AccountType = AccountType;
        this.transactionHistory = new ArrayList<>();
    }

    // getters
    public UUID getAccountNumber() {
        return AccountNumber;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public BigDecimal getBalance() {
        return Balance;
    }

    public LocalDate getDateCreated() {
        return DateCreated;
    }

    public AccountType getAccountType() {
        return AccountType;
    }

    public void deposit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be 0 or a negative amount");
        }

        this.Balance = Balance.add(amount);
        Transaction depositTransaction = new Transaction(TransactionType.DEPOSIT, amount, "Deposit of $" + amount);
        transactionHistory.add(depositTransaction);
    }

    public boolean withdraw(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be 0 or a negative amount");
        }

        if (!AccountType.canWithdraw(Balance, amount)) {
            return false;
        }
        Balance = Balance.subtract(amount);
        Transaction withdraw = new Transaction(TransactionType.WITHDRAWAL, amount, "Withdraw $" + amount);
        transactionHistory.add(withdraw);

        return true;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public boolean transferTo(Account destinationAccount, BigDecimal amount) {

        // Validation 1: Amount must be positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        // Validation 2: Destination account must exist and be different
        if (destinationAccount == null) {
            return false;
        }

        // Validation 3: Can't transfer to the same account
        if (this.AccountNumber.equals(destinationAccount.AccountNumber)) {
            return false;
        }

        // Validation 4: Source account must be able to withdraw this amount
        if (!this.AccountType.canWithdraw(this.Balance, amount)) {
            return false;
        }

        // PHASE 2: EXECUTE (all validations passed, safe to proceed)
        this.Balance = this.Balance.subtract(amount);
        destinationAccount.Balance = destinationAccount.Balance.add(amount);

        Transaction sourceTransaction = new Transaction(TransactionType.TRANSFER, amount, "transfer to " + destinationAccount.CustomerName);
        Transaction destinationAccountTransaction = new Transaction(TransactionType.TRANSFER, amount, "Transfer from " + CustomerName);

        transactionHistory.add(sourceTransaction);
        destinationAccount.transactionHistory.add(destinationAccountTransaction);

        return true;
    }

    public BigDecimal applyInterest() {

        BigDecimal interest = Balance.multiply(AccountType.getInterestRate());
        Balance = Balance.add(interest);

        Transaction applyInterestTransaction = new Transaction(TransactionType.INTEREST, interest, "Interest amount " + interest);
        transactionHistory.add(applyInterestTransaction);

        return interest;

    }
}
