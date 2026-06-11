package SecureMiddleware.audit;

import java.time.Instant;

public record AuditLogEntry(
        String correlationId,
        String method,
        String path,
        int status,
        long durationMs,
        Instant timestamp
) {
}
