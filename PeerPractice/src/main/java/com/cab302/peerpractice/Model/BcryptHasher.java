package com.cab302.peerpractice.Model;
import org.mindrot.jbcrypt.BCrypt;

public class BcryptHasher implements PasswordHasher {

    public String hasher(String raw) {
        return BCrypt.hashpw(raw,BCrypt.gensalt());
    }

    public boolean matches(String raw, String hash) {
        return BCrypt.checkpw(raw,hash);
    }
}
