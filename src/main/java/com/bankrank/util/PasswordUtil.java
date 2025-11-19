package com.bankrank.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility class for secure PIN/password hashing and verification using BCrypt.
 */
public class PasswordUtil {

    /**
     * Hashes a PIN using BCrypt.
     *
     * @param pin The plain text PIN to hash
     * @return The BCrypt hash of the PIN
     */
    public static String hashPin(String pin) {
        // BCrypt.gensalt() creates a random salt
        // 10 is the "cost" factor (higher = slower/more secure)
        return BCrypt.hashpw(pin, BCrypt.gensalt(10));
    }

    /**
     * Verifies a PIN against a stored hash.
     *
     * @param pin The plain text PIN to verify
     * @param hash The stored BCrypt hash
     * @return true if PIN matches, false otherwise
     */
    public static boolean verifyPin(String pin, String hash) {
        return BCrypt.checkpw(pin, hash);
    }
}
