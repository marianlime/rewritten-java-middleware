package SecureMiddleware.risk;

import java.util.List;

public record RiskEvaluationResponse(
        String clientId,
        int riskScore,
        String riskLevel,
        List<String> reasons,
        int recentSimilarRequests
) {
}
