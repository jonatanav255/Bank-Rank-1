package com.bankrank.model;

/**
 * User roles in the banking system.
 * Defines permission levels from customer (lowest) to admin (highest).
 */
public enum Role {
    /**
     * Regular customer - can only access their own accounts
     */
    CUSTOMER,

    /**
     * Bank teller - can create accounts and perform transactions for customers
     */
    TELLER,

    /**
     * Bank manager - teller permissions + reports and admin functions
     */
    MANAGER,

    /**
     * System administrator - full access including user management
     */
    ADMIN;

    /**
     * Check if this role has at least the given permission level.
     * Used for hierarchical permission checks.
     *
     * @param required The minimum required role
     * @return true if this role has sufficient permissions
     */
    public boolean hasPermission(Role required) {
        return this.ordinal() >= required.ordinal();
    }
}
