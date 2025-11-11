package com.bankrank.model;

import java.util.UUID;
import java.time.LocalDate;

// Create new accounts with unique account numbers, customer name, and initial deposit
// Support two account types: Savings and Checking
// Each account tracks balance, account holder info, and creation date
// Implement proper encapsulation - balance shouldn't be directly accessible
public class Account {

    private UUID AccountNumber;
    private String CustomerName;
    private double Balance;
    private LocalDate DateCreated;

    //constructor
    public Account(UUID AccountNumber, String CustomerName, double InitialValue) {
        this.AccountNumber = AccountNumber;
        this.CustomerName = CustomerName;
        this.Balance = InitialValue;
        this.DateCreated = LocalDate.now();
    }

    // getters
    public UUID getAccountNumber() {
        return AccountNumber;
    }

    public String getCustomerName() {
        return CustomerName;
    }

    public double getBalance() {
        return Balance;
    }

    public LocalDate getDateCreated() {
        return DateCreated;
    }

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount cannot be 0 or a negative amount");
        }

        this.Balance = Balance + amount;
    }
}
