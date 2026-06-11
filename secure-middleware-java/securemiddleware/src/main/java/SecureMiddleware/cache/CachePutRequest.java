package SecureMiddleware.cache;

public record CachePutRequest(
        String key,
        String value,
        long ttlSeconds
) {
}
