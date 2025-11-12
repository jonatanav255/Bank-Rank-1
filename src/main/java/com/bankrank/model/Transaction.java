package com.bankrank.model;

import java.time.LocalDateTime;

//   - Transaction type (String: "DEPOSIT", "WITHDRAWAL", "TRANSFER")
//   - Amount (double)
//   - Date/time (LocalDateTime)
//   - Description (String)
//   - Constructor that takes all these fields
//   - Getters for all fields
public class Transaction {

    private TransactionType type;
    private double amount;
    private LocalDateTime date;
    private String description;

    public Transaction(TransactionType type, double amount, String description) {
        this.type = type;
        this.amount = amount;
        this.date = LocalDateTime.now();
        this.description = description;
    }

//     getters
    public TransactionType getTransactionType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getDateTime() {
        return date;
    }

    public String getDescription() {
        return description;
    }

//     setters
}
