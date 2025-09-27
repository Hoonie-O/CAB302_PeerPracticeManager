package com.cab302.peerpractice.Model;
import org.mindrot.jbcrypt.BCrypt;

public class BcryptHasher implements PasswordHasher {

    public String hasher(String raw) {
        return BCrypt.hashpw(raw,BCrypt.gensalt());
    }

    public boolean matches(String raw, String hash) {
        if(raw == null || hash == null || hash.isEmpty() || !hash.startsWith("$2") || hash.length() < 60){
            return false;
        }
        try {
            return BCrypt.checkpw(raw, hash);
        } catch (Exception e) {
            return false;
        }
    }
}
