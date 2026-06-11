package SecureMiddleware.crypto;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class CryptoService {

    private static final String AES = "AES";
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE_BITS = 256;
    private static final int IV_SIZE_BYTES = 12;
    private static final int TAG_SIZE_BITS = 128;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES);
            keyGenerator.init(KEY_SIZE_BITS);
            this.secretKey = keyGenerator.generateKey();
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialise AES-GCM key", exception);
        }
    }

    public EncryptionResponse encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_SIZE_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_SIZE_BITS, iv));

            byte[] encryptedBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            return new EncryptionResponse(
                    Base64.getEncoder().encodeToString(encryptedBytes),
                    Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Encryption failed", exception);
        }
    }

    public DecryptionResponse decrypt(String ciphertext, String iv) {
        try {
            byte[] ciphertextBytes = Base64.getDecoder().decode(ciphertext);
            byte[] ivBytes = Base64.getDecoder().decode(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_SIZE_BITS, ivBytes));

            byte[] plaintextBytes = cipher.doFinal(ciphertextBytes);

            return new DecryptionResponse(new String(plaintextBytes, StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Decryption failed", exception);
        }
    }
}
