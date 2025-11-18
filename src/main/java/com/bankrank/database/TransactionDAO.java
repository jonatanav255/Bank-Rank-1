package com.bankrank.database;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bankrank.model.Transaction;
import com.bankrank.model.TransactionType;

public class TransactionDAO {

    List<Transaction> searchTransactions(UUID accountId, String description, TransactionType type, BigDecimal minAmount, BigDecimal maxAmount) throws SQLException {
        List<Transaction> searchTransactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                """
            SELECT t.*, a.customer_name, a.id as account_id
  FROM transactions t
  JOIN accounts a ON t.account_id = a.id
  WHERE 1=1
        """
        );

        if (accountId != null) {
            sql.append(" AND t.account_id = ?");
        }
        if (description != null && !description.trim().isEmpty()) {
            sql.append(" AND t.description ILIKE ?");
        }
        if (type != null) {
            sql.append(" AND t.transaction_type = ?");
        }
        if (minAmount != null) {
            sql.append(" AND t.amount >= ?");
        }
        if (maxAmount != null) {
            sql.append(" AND t.amount <= ?");
        }

        return searchTransactions;
    }
}
