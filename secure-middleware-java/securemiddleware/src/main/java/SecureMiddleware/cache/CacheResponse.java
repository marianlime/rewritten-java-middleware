package SecureMiddleware.cache;

public record CacheResponse(
        String key,
        String value,
        boolean hit,
        long expiresAtMillis
) {
}
