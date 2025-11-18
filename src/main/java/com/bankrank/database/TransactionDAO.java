package com.bankrank.database;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bankrank.model.Transaction;
import com.bankrank.model.TransactionType;

public class TransactionDAO {

    List<Transaction> searchTransactions(UUID accountId, String description, TransactionType type, BigDecimal minAmount, BigDecimal maxAmount) {
        List<Transaction> searchTransactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                """
          WHERE 1=1SELECT t.*, a.customer_name, a.id as account_id
          FROM transactions t
          JOIN accounts a ON t.account_id = a.id
        """
        );

        if (accountId != null) {
            sql.append(" AND accountId = ?");
        }
        if (description != null && !description.trim().isEmpty()) {
            sql.append(" AND customer_name ILIKE ?");
        }
        if (type != null) {
            sql.append(" AND type = ?");
        }
        if (minAmount != null) {
            sql.append(" AND minAmount >= ?");
        }
        if (maxAmount != null) {
            sql.append(" AND maxAmount <= ?");
        }

        return searchTransactions;
    }
}
