package SecureMiddleware.risk;

import SecureMiddleware.audit.AuditLogEntry;
import SecureMiddleware.audit.AuditLogService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class RiskScoringService {

    private static final int RECENT_WINDOW_SECONDS = 60;

    private final AuditLogService auditLogService;

    public RiskScoringService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public RiskEvaluationResponse evaluate(RiskEvaluationRequest request) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        String clientId = normaliseClientId(request.clientId());
        String path = normalisePath(request.path());
        String method = normaliseMethod(request.method());

        if (request.rateLimitExceeded()) {
            score += 40;
            reasons.add("Client exceeded sliding-window rate limit");
        }

        if (request.failedAuthAttempts() >= 5) {
            score += 35;
            reasons.add("High number of failed authentication attempts");
        } else if (request.failedAuthAttempts() >= 3) {
            score += 25;
            reasons.add("Repeated failed authentication attempts");
        } else if (request.failedAuthAttempts() > 0) {
            score += 10;
            reasons.add("Recent failed authentication attempt");
        }

        if (isSensitiveEndpoint(path)) {
            score += 15;
            reasons.add("Sensitive endpoint accessed");
        }

        if (isSuspiciousPath(path)) {
            score += 30;
            reasons.add("Suspicious path pattern detected");
        }

        if (isStateChangingMethod(method)) {
            score += 10;
            reasons.add("State-changing HTTP method used");
        }

        int recentSimilarRequests = countRecentSimilarRequests(path);

        if (recentSimilarRequests >= 10) {
            score += 25;
            reasons.add("High number of similar requests in recent audit history");
        } else if (recentSimilarRequests >= 5) {
            score += 15;
            reasons.add("Repeated similar requests in recent audit history");
        }

        score = Math.min(score, 100);

        if (reasons.isEmpty()) {
            reasons.add("No suspicious indicators detected");
        }

        return new RiskEvaluationResponse(
                clientId,
                score,
                riskLevel(score),
                reasons,
                recentSimilarRequests
        );
    }

    private String normaliseClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return "unknown-client";
        }
        return clientId;
    }

    private String normalisePath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        return path.toLowerCase();
    }

    private String normaliseMethod(String method) {
        if (method == null || method.isBlank()) {
            return "GET";
        }
        return method.toUpperCase();
    }

    private boolean isSensitiveEndpoint(String path) {
        return path.contains("/auth")
                || path.contains("/crypto/decrypt")
                || path.contains("/gateway")
                || path.contains("/admin");
    }

    private boolean isSuspiciousPath(String path) {
        return path.contains("..")
                || path.contains("%2e%2e")
                || path.contains("/etc/passwd")
                || path.contains("cmd=")
                || path.contains("select%20")
                || path.contains("<script");
    }

    private boolean isStateChangingMethod(String method) {
        return method.equals("POST")
                || method.equals("PUT")
                || method.equals("PATCH")
                || method.equals("DELETE");
    }

    private int countRecentSimilarRequests(String path) {
        Instant cutoff = Instant.now().minusSeconds(RECENT_WINDOW_SECONDS);

        int count = 0;

        for (AuditLogEntry entry : auditLogService.recent()) {
            if (entry.timestamp().isAfter(cutoff)
                    && entry.path() != null
                    && entry.path().toLowerCase().equals(path)) {
                count++;
            }
        }

        return count;
    }

    private String riskLevel(int score) {
        if (score >= 75) {
            return "HIGH";
        }

        if (score >= 40) {
            return "MEDIUM";
        }

        return "LOW";
    }
}
