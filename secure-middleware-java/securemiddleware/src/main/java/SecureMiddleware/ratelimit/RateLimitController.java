package SecureMiddleware.ratelimit;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rate-limit")
public class RateLimitController {

    private final SlidingWindowRateLimiterService rateLimiterService;

    public RateLimitController(SlidingWindowRateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @PostMapping("/check")
    public RateLimitResponse check(@RequestBody RateLimitRequest request) {
        return rateLimiterService.check(request.clientId());
    }
}
