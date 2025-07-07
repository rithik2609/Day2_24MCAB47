package com.example.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordUtil {

    // Hash a password
    public static String hashPassword(String password) {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    // Verify password against hash
    public static boolean verifyPassword(String password, String hashedPassword) {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified;
    }
}
