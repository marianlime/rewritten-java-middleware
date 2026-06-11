package SecureMiddleware.risk;

import SecureMiddleware.audit.AuditLogService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RiskScoringServiceTest {

    @Test
    void shouldReturnHighRiskForRateLimitAndFailedAuth() {
        AuditLogService auditLogService = new AuditLogService();
        RiskScoringService service = new RiskScoringService(auditLogService);

        RiskEvaluationRequest request = new RiskEvaluationRequest(
                "client-123",
                "POST",
                "/api/v1/crypto/decrypt",
                true,
                3
        );

        RiskEvaluationResponse response = service.evaluate(request);

        assertEquals("client-123", response.clientId());
        assertEquals("HIGH", response.riskLevel());
        assertTrue(response.riskScore() >= 75);
        assertFalse(response.reasons().isEmpty());
    }

    @Test
    void shouldReturnLowRiskForNormalGetRequest() {
        AuditLogService auditLogService = new AuditLogService();
        RiskScoringService service = new RiskScoringService(auditLogService);

        RiskEvaluationRequest request = new RiskEvaluationRequest(
                "client-123",
                "GET",
                "/api/v1/cache/client-123",
                false,
                0
        );

        RiskEvaluationResponse response = service.evaluate(request);

        assertEquals("LOW", response.riskLevel());
        assertTrue(response.riskScore() < 40);
    }

    @Test
    void shouldDetectSuspiciousPathPatterns() {
        AuditLogService auditLogService = new AuditLogService();
        RiskScoringService service = new RiskScoringService(auditLogService);

        RiskEvaluationRequest request = new RiskEvaluationRequest(
                "client-123",
                "GET",
                "/api/v1/gateway/../../etc/passwd",
                false,
                0
        );

        RiskEvaluationResponse response = service.evaluate(request);

        assertTrue(response.riskScore() >= 30);
        assertTrue(response.reasons().stream().anyMatch(reason -> reason.contains("Suspicious path")));
    }
}
