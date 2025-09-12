package com.cab302.peerpractice.Model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SimplePasswordHasher implements PasswordHasher {

    @Override
    public String hasher(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(raw.getBytes());
            return Base64.getEncoder().encodeToString(hashBytes); // store as base64
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    @Override
    public boolean matches(String raw, String hash) {
        return hasher(raw).equals(hash);
    }
}
