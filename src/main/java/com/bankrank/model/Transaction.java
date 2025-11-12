package com.bankrank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;

    public Transaction(TransactionType type, BigDecimal amount, String description) {
        this.type = type;
        this.amount = amount;
        this.date = LocalDateTime.now();
        this.description = description;
    }

    public TransactionType getTransactionType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getDateTime() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
