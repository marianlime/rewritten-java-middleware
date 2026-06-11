package SecureMiddleware.ratelimit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SlidingWindowRateLimiterServiceTest {

    @Test
    void shouldAllowRequestsWithinLimit() {
        SlidingWindowRateLimiterService service = new SlidingWindowRateLimiterService();

        for (int i = 0; i < 5; i++) {
            RateLimitResponse response = service.check("client-123");
            assertTrue(response.allowed());
        }
    }

    @Test
    void shouldBlockRequestsAfterLimitExceeded() {
        SlidingWindowRateLimiterService service = new SlidingWindowRateLimiterService();

        for (int i = 0; i < 5; i++) {
            assertTrue(service.check("client-123").allowed());
        }

        RateLimitResponse blocked = service.check("client-123");

        assertFalse(blocked.allowed());
        assertEquals(0, blocked.remainingRequests());
        assertTrue(blocked.retryAfterSeconds() > 0);
    }

    @Test
    void shouldTrackDifferentClientsSeparately() {
        SlidingWindowRateLimiterService service = new SlidingWindowRateLimiterService();

        for (int i = 0; i < 5; i++) {
            assertTrue(service.check("client-a").allowed());
        }

        assertFalse(service.check("client-a").allowed());
        assertTrue(service.check("client-b").allowed());
    }
}
