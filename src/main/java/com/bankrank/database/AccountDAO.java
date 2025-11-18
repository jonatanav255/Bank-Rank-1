package com.bankrank.database;

import com.bankrank.model.Account;
import com.bankrank.model.AccountType;
import com.bankrank.model.CheckingAccountType;
import com.bankrank.model.SavingsAccountType;
import com.bankrank.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bankrank.model.TransactionType;

/**
 * Data Access Object for Account entity. Handles all database operations
 * related to accounts and transactions.
 */
public class AccountDAO {

    /**
     * Saves a new account to the database along with all its transactions. Uses
     * database transaction to ensure atomicity.
     */
    public void save(Account account) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // Start transaction

            // Save account
            String accountSql = "INSERT INTO accounts (id, customer_name, balance, date_created, account_type) "
                    + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(accountSql)) {
                stmt.setObject(1, account.getAccountNumber());
                stmt.setString(2, account.getCustomerName());
                stmt.setBigDecimal(3, account.getBalance());
                stmt.setTimestamp(4, Timestamp.valueOf(account.getDateCreated().atStartOfDay()));
                stmt.setString(5, getAccountTypeName(account.getAccountType()));
                stmt.executeUpdate();
            }

            // Save transactions
            saveTransactions(conn, account.getAccountNumber(), account.getTransactionHistory());

            conn.commit();  // Commit transaction
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();  // Rollback on error
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Updates an existing account in the database.
     */
    public void update(Account account) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Update account
            String sql = "UPDATE accounts SET customer_name = ?, balance = ?, account_type = ? WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, account.getCustomerName());
                stmt.setBigDecimal(2, account.getBalance());
                stmt.setString(3, getAccountTypeName(account.getAccountType()));
                stmt.setObject(4, account.getAccountNumber());
                stmt.executeUpdate();
            }

            // Delete old transactions and save new ones
            deleteTransactions(conn, account.getAccountNumber());
            saveTransactions(conn, account.getAccountNumber(), account.getTransactionHistory());

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    /**
     * Finds an account by its ID.
     */
    public Account findById(UUID accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                List<Transaction> transactions = loadTransactions(conn, accountId);
                Account account = mapResultSetToAccount(rs, transactions);

                return account;
            }
            return null;
        }
    }

    /**
     * Returns all accounts in the database.
     */
    public List<Account> findAll() throws SQLException {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";

        try (Connection conn = DatabaseConnection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UUID accountId = (UUID) rs.getObject("id");
                List<Transaction> transactions = loadTransactions(conn, accountId);
                Account account = mapResultSetToAccount(rs, transactions);
                accounts.add(account);
            }
        }
        return accounts;
    }

    public List<Account> searchAccounts(String customerName, String accountType) throws SQLException {
        List<Account> accounts = new ArrayList<>();

        // Build dynamic SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM accounts WHERE 1=1");

        // Add filters if provided
        if (customerName != null && !customerName.trim().isEmpty()) {
            sql.append(" AND customer_name ILIKE ?");
        }
        if (accountType != null && !accountType.trim().isEmpty()) {
            sql.append(" AND account_type = ?");
        }

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Bind parameters in order
            int paramIndex = 1;
            if (customerName != null && !customerName.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + customerName.trim() + "%");
            }
            if (accountType != null && !accountType.trim().isEmpty()) {
                stmt.setString(paramIndex++, accountType.trim().toUpperCase());
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                UUID accountId = (UUID) rs.getObject("id");
                List<Transaction> transactions = loadTransactions(conn, accountId);
                Account account = mapResultSetToAccount(rs, transactions);
                accounts.add(account);
            }
        }
        return accounts;
    }

    /**
     * Deletes an account and all its transactions.
     */
    public void delete(UUID accountId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Delete transactions first (foreign key constraint)
            deleteTransactions(conn, accountId);

            // Delete account
            String sql = "DELETE FROM accounts WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setObject(1, accountId);
                stmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // Helper methods
    private void saveTransactions(Connection conn, UUID accountId, List<Transaction> transactions)
            throws SQLException {
        String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, transaction_date) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Transaction transaction : transactions) {
                stmt.setObject(1, accountId);
                stmt.setString(2, transaction.getTransactionType().name());
                stmt.setBigDecimal(3, transaction.getAmount());
                stmt.setString(4, transaction.getDescription());
                stmt.setTimestamp(5, Timestamp.valueOf(transaction.getDateTime()));
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void deleteTransactions(Connection conn, UUID accountId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE account_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, accountId);
            stmt.executeUpdate();
        }
    }

    private List<Transaction> loadTransactions(Connection conn, UUID accountId) throws SQLException {
        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY transaction_date";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, accountId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                String typeString = rs.getString("transaction_type");
                BigDecimal amount = rs.getBigDecimal("amount");
                String description = rs.getString("description");
                LocalDateTime dateTime = rs.getTimestamp("transaction_date").toLocalDateTime();
                TransactionType type = TransactionType.valueOf(typeString);

                Transaction transaction = new Transaction(type, amount, description, dateTime);
                transactions.add(transaction);
            }
            return transactions;
        }
    }

    private Account mapResultSetToAccount(ResultSet rs, List<Transaction> transactions) throws SQLException {

        UUID id = (UUID) rs.getObject("id");
        String customerName = rs.getString("customer_name");
        BigDecimal balance = rs.getBigDecimal("balance");
        LocalDate dateCreated = rs.getTimestamp("date_created").toLocalDateTime().toLocalDate();
        String accountTypeName = rs.getString("account_type");

        AccountType accountType = createAccountType(accountTypeName);

        return new Account(id, customerName, balance, accountType, dateCreated, transactions);
    }

    private AccountType createAccountType(String typeName) {
        return switch (typeName) {
            case "SAVINGS" ->
                new SavingsAccountType();
            case "CHECKING" ->
                new CheckingAccountType();
            default ->
                throw new IllegalArgumentException("Unknown account type: " + typeName);
        };
    }

    private String getAccountTypeName(AccountType accountType) {
        if (accountType instanceof SavingsAccountType) {
            return "SAVINGS";
        } else if (accountType instanceof CheckingAccountType) {
            return "CHECKING";
        }
        throw new IllegalArgumentException("Unknown account type");
    }
}
