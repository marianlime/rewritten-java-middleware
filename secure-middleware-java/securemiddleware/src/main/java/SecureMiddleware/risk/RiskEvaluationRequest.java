package SecureMiddleware.risk;

public record RiskEvaluationRequest(
        String clientId,
        String method,
        String path,
        boolean rateLimitExceeded,
        int failedAuthAttempts
) {
}
