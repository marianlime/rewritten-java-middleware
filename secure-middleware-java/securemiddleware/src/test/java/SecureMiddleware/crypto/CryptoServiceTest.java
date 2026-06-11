package SecureMiddleware.crypto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "crypto.aes-key-base64=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY="
})
class CryptoServiceTest {

    @Autowired
    private CryptoService cryptoService;

    @Test
    void shouldEncryptAndDecryptPlaintext() {
        String plaintext = "hello banking security";

        EncryptionResponse encrypted = cryptoService.encrypt(plaintext);
        DecryptionResponse decrypted = cryptoService.decrypt(encrypted.ciphertext(), encrypted.iv());

        assertNotNull(encrypted.ciphertext());
        assertNotNull(encrypted.iv());
        assertNotEquals(plaintext, encrypted.ciphertext());
        assertEquals(plaintext, decrypted.plaintext());
    }

    @Test
    void shouldGenerateDifferentCiphertextsForSamePlaintext() {
        String plaintext = "same input";

        EncryptionResponse first = cryptoService.encrypt(plaintext);
        EncryptionResponse second = cryptoService.encrypt(plaintext);

        assertNotEquals(first.ciphertext(), second.ciphertext());
        assertNotEquals(first.iv(), second.iv());
    }
}
