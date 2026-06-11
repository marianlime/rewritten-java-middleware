package SecureMiddleware.auth;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String SECRET = "change-this-demo-secret-for-real-deployments-change-this";
    private static final long EXPIRY_SECONDS = 3600;

    public String generateToken(String username) {
        long now = Instant.now().getEpochSecond();
        long expiry = now + EXPIRY_SECONDS;

        String header = base64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64Url("""
                {"sub":"%s","iat":%d,"exp":%d}
                """.formatted(username, now, expiry).trim());

        String unsignedToken = header + "." + payload;
        String signature = sign(unsignedToken);

        return unsignedToken + "." + signature;
    }

    public boolean isValid(String token) {
        try {
            String[] parts = token.split("\\.");

            if (parts.length != 3) {
                return false;
            }

            String unsignedToken = parts[0] + "." + parts[1];
            String expectedSignature = sign(unsignedToken);

            if (!constantTimeEquals(expectedSignature, parts[2])) {
                return false;
            }

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            long expiry = extractExpiry(payloadJson);

            return Instant.now().getEpochSecond() < expiry;
        } catch (Exception exception) {
            return false;
        }
    }

    public String getUsername(String token) {
        String[] parts = token.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

        String marker = "\"sub\":\"";
        int start = payloadJson.indexOf(marker) + marker.length();
        int end = payloadJson.indexOf("\"", start);

        return payloadJson.substring(start, end);
    }

    public long expirySeconds() {
        return EXPIRY_SECONDS;
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));

            byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign JWT", exception);
        }
    }

    private String base64Url(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private long extractExpiry(String payloadJson) {
        String marker = "\"exp\":";
        int start = payloadJson.indexOf(marker) + marker.length();
        int end = payloadJson.indexOf("}", start);

        return Long.parseLong(payloadJson.substring(start, end).trim());
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.getBytes(StandardCharsets.UTF_8),
                right.getBytes(StandardCharsets.UTF_8)
        );
    }
}
