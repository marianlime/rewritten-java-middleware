package SecureMiddleware.crypto;

public record DecryptionRequest(String ciphertext, String iv) {
}
