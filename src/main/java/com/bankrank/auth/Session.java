package com.bankrank.auth;

import com.bankrank.model.User;

import java.time.LocalDateTime;

/**
 * Singleton session manager that tracks the currently logged-in user.
 * In a real web application, this would be handled by HTTP sessions,
 * but for a console app, we use a simple singleton.
 */
public class Session {
    private static Session instance;

    private User currentUser;
    private LocalDateTime loginTime;

    /**
     * Private constructor (singleton pattern).
     */
    private Session() {
        this.currentUser = null;
        this.loginTime = null;
    }

    /**
     * Get the singleton instance.
     */
    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    /**
     * Log in a user.
     */
    public void login(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
    }

    /**
     * Log out the current user.
     */
    public void logout() {
        this.currentUser = null;
        this.loginTime = null;
    }

    /**
     * Check if a user is currently logged in.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Get the currently logged-in user.
     * @return Current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Get the login time.
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Require that a user is logged in.
     * @throws IllegalStateException if no user is logged in
     */
    public void requireLogin() {
        if (!isLoggedIn()) {
            throw new IllegalStateException("You must be logged in to perform this action");
        }
    }

    /**
     * Get current user (throws exception if not logged in).
     */
    public User getCurrentUserOrThrow() {
        requireLogin();
        return currentUser;
    }
}
