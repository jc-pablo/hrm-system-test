package hrms.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Salted PBKDF2-HMAC-SHA256 password hashing using Java's standard cryptography APIs. */
public final class PasswordUtil {
    private static final int ITERATIONS = 210_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String PREFIX = "PBKDF2-SHA256";

    private PasswordUtil() { }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] derived = derive(password.toCharArray(), salt, ITERATIONS);
        return PREFIX + "$" + ITERATIONS + "$" + Base64.getEncoder().withoutPadding().encodeToString(salt)
                + "$" + Base64.getEncoder().withoutPadding().encodeToString(derived);
    }

    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) return false;
        try {
            String[] parts = storedHash.split("\\$", -1);
            if (parts.length != 4 || !PREFIX.equals(parts[0])) return false;
            int iterations = Integer.parseInt(parts[1]);
            if (iterations < 100_000 || iterations > 2_000_000) return false;
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password.toCharArray(), salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derive(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("PBKDF2-HMAC-SHA256 is not available in this Java runtime.", ex);
        } finally {
            spec.clearPassword();
            java.util.Arrays.fill(password, '\0');
        }
    }
}
