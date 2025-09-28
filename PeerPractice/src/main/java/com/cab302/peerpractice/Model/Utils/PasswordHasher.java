package com.cab302.peerpractice.Model.Utils;

public interface PasswordHasher {
    String hasher(String raw);
    boolean matches(String raw, String hash);
}
