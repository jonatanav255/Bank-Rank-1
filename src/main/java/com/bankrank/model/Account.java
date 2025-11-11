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

    //   constructor
    public Account(UUID AccountNumber, String CustomerName, double InitialValue) {
        this.AccountNumber = AccountNumber;
        this.CustomerName = CustomerName;
        this.Balance = InitialValue;
        this.DateCreated = LocalDate.now();
    }

    // getters
//   Start with this:
    public double getAccountNumber() {
        return Balance;
    }
    public String getCustomerName() {
        return CustomerName;
    }
//   Write getter methods for all four fields. Use proper Java naming conventions:
//   - getAccountNumber()
//   - getCustomerName()
//   - getBalance()
//   - getDateCreated()
    // setters
    // methods
//     balance initial deposit account types - Savings and Checking
}
