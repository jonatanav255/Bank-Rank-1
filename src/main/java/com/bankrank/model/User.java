package com.bankrank.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a user in the banking system.
 * Users can be customers, tellers, managers, or administrators.
 */
public class User {
    private final UUID id;
    private final String username;
    private final String passwordHash;
    private String fullName;
    private Role role;
    private boolean isActive;
    private final LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    /**
     * Constructor for creating a new user.
     */
    public User(UUID id, String username, String passwordHash, String fullName, Role role) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.lastLogin = null;
    }

    /**
     * Constructor for loading existing user from database.
     */
    public User(UUID id, String username, String passwordHash, String fullName, Role role,
                boolean isActive, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    // Setters for mutable fields
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    /**
     * Check if this user has the required permission level.
     */
    public boolean hasPermission(Role required) {
        return this.role.hasPermission(required);
    }

    /**
     * Check if this user is a customer (not staff).
     */
    public boolean isCustomer() {
        return this.role == Role.CUSTOMER;
    }

    /**
     * Check if this user is staff (teller or higher).
     */
    public boolean isStaff() {
        return this.role.hasPermission(Role.TELLER);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", isActive=" + isActive +
                '}';
    }
}
