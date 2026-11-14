package br.com.pizzadelivery.auth;

import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class CustomerAccountService {
    private static final int ITERATIONS = 120000;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 256;

    private final ConcurrentMap<String, AccountCredentials> accounts = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    public AccountProfile register(String displayName, String email, String password) {
        String normalizedName = displayName == null ? "" : displayName.trim();
        String normalizedEmail = normalizeEmail(email);
        if (normalizedName.isEmpty() || normalizedName.length() > 80) {
            throw new IllegalArgumentException("Informe um nome com até 80 caracteres.");
        }
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
        if (password == null || password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("A senha deve ter entre 8 e 72 caracteres.");
        }

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        AccountCredentials credentials = new AccountCredentials(
                normalizedName, salt, hashPassword(password, salt));
        if (accounts.putIfAbsent(normalizedEmail, credentials) != null) {
            throw new IllegalArgumentException("Este e-mail já possui uma conta.");
        }
        return new AccountProfile(normalizedName, normalizedEmail);
    }

    public Optional<AccountProfile> authenticate(String email, String password) {
        if (password == null || password.isEmpty()) {
            return Optional.empty();
        }
        String normalizedEmail = normalizeEmail(email);
        AccountCredentials credentials = accounts.get(normalizedEmail);
        if (credentials == null) {
            return Optional.empty();
        }

        byte[] candidateHash = hashPassword(password, credentials.salt);
        if (!MessageDigest.isEqual(credentials.passwordHash, candidateHash)) {
            return Optional.empty();
        }
        return Optional.of(new AccountProfile(credentials.displayName, normalizedEmail));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private byte[] hashPassword(String password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, HASH_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Não foi possível verificar a senha.", ex);
        } finally {
            spec.clearPassword();
        }
    }

    private static final class AccountCredentials {
        private final String displayName;
        private final byte[] salt;
        private final byte[] passwordHash;

        private AccountCredentials(String displayName, byte[] salt, byte[] passwordHash) {
            this.displayName = displayName;
            this.salt = salt;
            this.passwordHash = passwordHash;
        }
    }

    public static final class AccountProfile {
        private final String displayName;
        private final String email;

        private AccountProfile(String displayName, String email) {
            this.displayName = displayName;
            this.email = email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmail() {
            return email;
        }
    }
}
