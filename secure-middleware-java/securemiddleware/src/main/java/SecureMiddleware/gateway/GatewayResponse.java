package SecureMiddleware.gateway;

public record GatewayResponse(
        int status,
        String body,
        boolean cacheHit
) {
}
