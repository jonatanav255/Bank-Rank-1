package com.bankrank.auth;

import com.bankrank.database.UserDAO;
import com.bankrank.model.Role;
import com.bankrank.model.User;
import com.bankrank.util.PasswordUtil;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Handles user authentication and authorization.
 */
public class AuthenticationService {
    private final UserDAO userDAO;
    private final Session session;

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.session = Session.getInstance();
    }

    /**
     * Authenticate a user with username and password.
     *
     * @param username The username
     * @param password The plain text password
     * @return true if login successful, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean login(String username, String password) throws SQLException {
        // Find user by username
        User user = userDAO.findByUsername(username);

        if (user == null) {
            return false;  // User not found
        }

        // Check if user is active
        if (!user.isActive()) {
            System.out.println("⚠ Account is disabled. Contact administrator.");
            return false;
        }

        // Verify password
        if (!PasswordUtil.verifyPin(password, user.getPasswordHash())) {
            return false;  // Wrong password
        }

        // Login successful
        session.login(user);
        userDAO.updateLastLogin(user.getId());

        return true;
    }

    /**
     * Log out the current user.
     */
    public void logout() {
        session.logout();
    }

    /**
     * Check if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return session.isLoggedIn();
    }

    /**
     * Get the current logged-in user.
     */
    public User getCurrentUser() {
        return session.getCurrentUser();
    }

    /**
     * Check if current user has required permission level.
     *
     * @param required Minimum required role
     * @return true if user has permission
     */
    public boolean hasPermission(Role required) {
        if (!session.isLoggedIn()) {
            return false;
        }
        return session.getCurrentUser().hasPermission(required);
    }

    /**
     * Check if current user can access a specific account.
     * Staff can access all accounts, customers only their own.
     *
     * @param accountId The account to check
     * @return true if user can access this account
     * @throws SQLException if database error occurs
     */
    public boolean canAccessAccount(UUID accountId) throws SQLException {
        if (!session.isLoggedIn()) {
            return false;
        }

        User user = session.getCurrentUser();

        // Staff can access all accounts
        if (user.isStaff()) {
            return true;
        }

        // Customers can only access accounts they own
        return userDAO.userOwnsAccount(user.getId(), accountId);
    }

    /**
     * Require that current user has minimum permission level.
     *
     * @param required Minimum required role
     * @throws SecurityException if user doesn't have permission
     */
    public void requirePermission(Role required) {
        if (!hasPermission(required)) {
            throw new SecurityException("Insufficient permissions. Required: " + required);
        }
    }

    /**
     * Require that current user can access the specified account.
     *
     * @param accountId The account ID
     * @throws SecurityException if user cannot access account
     * @throws SQLException if database error occurs
     */
    public void requireAccountAccess(UUID accountId) throws SQLException {
        if (!canAccessAccount(accountId)) {
            throw new SecurityException("You do not have permission to access this account");
        }
    }
}
