package SecureMiddleware.crypto;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crypto")
public class CryptoController {

    private final CryptoService cryptoService;

    public CryptoController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @PostMapping("/encrypt")
    public EncryptionResponse encrypt(@RequestBody EncryptionRequest request) {
        return cryptoService.encrypt(request.plaintext());
    }

    @PostMapping("/decrypt")
    public DecryptionResponse decrypt(@RequestBody DecryptionRequest request) {
        return cryptoService.decrypt(request.ciphertext(), request.iv());
    }
}
