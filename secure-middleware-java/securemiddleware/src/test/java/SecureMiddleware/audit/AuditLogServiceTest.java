package SecureMiddleware.audit;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogServiceTest {

    @Test
    void shouldStoreRecentAuditLog() {
        AuditLogService service = new AuditLogService();

        AuditLogEntry entry = new AuditLogEntry(
                "correlation-1",
                "POST",
                "/api/v1/crypto/encrypt",
                200,
                12,
                Instant.now()
        );

        service.record(entry);

        List<AuditLogEntry> recent = service.recent();

        assertEquals(1, recent.size());
        assertEquals("correlation-1", recent.get(0).correlationId());
        assertEquals("/api/v1/crypto/encrypt", recent.get(0).path());
    }

    @Test
    void shouldKeepOnlyMostRecentOneHundredLogs() {
        AuditLogService service = new AuditLogService();

        for (int i = 0; i < 105; i++) {
            service.record(new AuditLogEntry(
                    "correlation-" + i,
                    "GET",
                    "/api/v1/test/" + i,
                    200,
                    i,
                    Instant.now()
            ));
        }

        List<AuditLogEntry> recent = service.recent();

        assertEquals(100, recent.size());
        assertEquals("correlation-104", recent.get(0).correlationId());
    }
}
