package com.bankrank.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private TransactionType type;
    private BigDecimal amount;
    private LocalDateTime date;
    private String description;

    public Transaction(TransactionType type, BigDecimal amount, String description, LocalDateTime date) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.date = date;
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
