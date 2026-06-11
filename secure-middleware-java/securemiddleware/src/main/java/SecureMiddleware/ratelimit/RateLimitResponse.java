package SecureMiddleware.ratelimit;

public record RateLimitResponse(
        String clientId,
        boolean allowed,
        int remainingRequests,
        long retryAfterSeconds,
        int limit,
        int windowSeconds
) {
}
