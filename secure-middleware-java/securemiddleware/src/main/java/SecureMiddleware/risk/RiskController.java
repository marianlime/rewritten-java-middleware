package SecureMiddleware.risk;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {

    private final RiskScoringService riskScoringService;

    public RiskController(RiskScoringService riskScoringService) {
        this.riskScoringService = riskScoringService;
    }

    @PostMapping("/evaluate")
    public RiskEvaluationResponse evaluate(@RequestBody RiskEvaluationRequest request) {
        return riskScoringService.evaluate(request);
    }
}
