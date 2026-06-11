package SecureMiddleware.cache;

public record CacheEntry(
        String key,
        String value,
        long createdAtMillis,
        long expiresAtMillis
) {
    public boolean expired(long nowMillis) {
        return nowMillis >= expiresAtMillis;
    }
}
