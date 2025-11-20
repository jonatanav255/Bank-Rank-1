package com.bankrank.database;

import com.bankrank.model.Role;
import com.bankrank.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Data Access Object for User entity.
 * Handles all database operations related to users and account ownership.
 */
public class UserDAO {

    /**
     * Save a new user to the database.
     */
    public void save(User user) throws SQLException {
        String sql = "INSERT INTO users (id, username, password_hash, full_name, role, is_active, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, user.getId());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getFullName());
            stmt.setString(5, user.getRole().name());
            stmt.setBoolean(6, user.isActive());
            stmt.setTimestamp(7, Timestamp.valueOf(user.getCreatedAt()));
            stmt.executeUpdate();
        }
    }

    /**
     * Find a user by username (for login).
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * Find a user by ID.
     */
    public User findById(UUID userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
            return null;
        }
    }

    /**
     * Get all users.
     */
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    /**
     * Update user information.
     */
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, role = ?, is_active = ?, last_login = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getRole().name());
            stmt.setBoolean(3, user.isActive());
            stmt.setTimestamp(4, user.getLastLogin() != null ? Timestamp.valueOf(user.getLastLogin()) : null);
            stmt.setObject(5, user.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Update last login timestamp.
     */
    public void updateLastLogin(UUID userId) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setObject(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Link a user to an account (make them an owner).
     */
    public void addAccountOwner(UUID accountId, UUID userId) throws SQLException {
        String sql = "INSERT INTO account_owners (account_id, user_id) VALUES (?, ?) "
                + "ON CONFLICT DO NOTHING";  // Ignore if already exists

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, accountId);
            stmt.setObject(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Remove a user from account ownership.
     */
    public void removeAccountOwner(UUID accountId, UUID userId) throws SQLException {
        String sql = "DELETE FROM account_owners WHERE account_id = ? AND user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, accountId);
            stmt.setObject(2, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get all account IDs owned by a user.
     */
    public List<UUID> getAccountsForUser(UUID userId) throws SQLException {
        List<UUID> accountIds = new ArrayList<>();
        String sql = "SELECT account_id FROM account_owners WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                accountIds.add((UUID) rs.getObject("account_id"));
            }
        }
        return accountIds;
    }

    /**
     * Get all user IDs who own an account.
     */
    public List<UUID> getOwnersForAccount(UUID accountId) throws SQLException {
        List<UUID> userIds = new ArrayList<>();
        String sql = "SELECT user_id FROM account_owners WHERE account_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, accountId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                userIds.add((UUID) rs.getObject("user_id"));
            }
        }
        return userIds;
    }

    /**
     * Check if a user owns a specific account.
     */
    public boolean userOwnsAccount(UUID userId, UUID accountId) throws SQLException {
        String sql = "SELECT 1 FROM account_owners WHERE user_id = ? AND account_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, userId);
            stmt.setObject(2, accountId);
            ResultSet rs = stmt.executeQuery();

            return rs.next();
        }
    }

    /**
     * Map ResultSet to User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        UUID id = (UUID) rs.getObject("id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        Role role = Role.valueOf(rs.getString("role"));
        boolean isActive = rs.getBoolean("is_active");
        LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();

        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        LocalDateTime lastLogin = lastLoginTs != null ? lastLoginTs.toLocalDateTime() : null;

        return new User(id, username, passwordHash, fullName, role, isActive, createdAt, lastLogin);
    }
}
