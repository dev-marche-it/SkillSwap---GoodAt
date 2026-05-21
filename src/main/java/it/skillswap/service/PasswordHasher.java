package it.skillswap.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash SHA-256 delle password (prototipo scolastico; in produzione usare BCrypt/Argon2).
 */
public final class PasswordHasher {

    private PasswordHasher() {}

    /**
     * @param plain password in chiaro
     * @return digest esadecimale SHA-256
     */
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 non disponibile", e);
        }
    }

    /**
     * @param plain password in chiaro
     * @param storedHash hash salvato
     * @return {@code true} se la password corrisponde
     */
    public static boolean matches(String plain, String storedHash) {
        if (plain == null || storedHash == null || storedHash.isBlank()) {
            return false;
        }
        return storedHash.equals(hash(plain));
    }
}
