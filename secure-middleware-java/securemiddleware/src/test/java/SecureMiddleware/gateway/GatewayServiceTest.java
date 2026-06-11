package SecureMiddleware.gateway;

import SecureMiddleware.cache.TtlLruCacheService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GatewayServiceTest {

    @Test
    void shouldDetectHopByHopHeaders() {
        GatewayService service = new GatewayService(new TtlLruCacheService());

        assertTrue(service.isHopByHopHeader("Connection"));
        assertTrue(service.isHopByHopHeader("Keep-Alive"));
        assertTrue(service.isHopByHopHeader("Transfer-Encoding"));
        assertFalse(service.isHopByHopHeader("X-Correlation-Id"));
    }

    @Test
    void shouldReturnCachedGatewayResponseWhenCacheEntryExists() {
        TtlLruCacheService cacheService = new TtlLruCacheService();
        GatewayService service = new GatewayService(cacheService);

        cacheService.put(
                "GET:echo?message=test",
                "{\"message\":\"cached response\"}",
                60
        );

        GatewayResponse response = service.forwardGet("echo", "message=test", "correlation-1");

        assertEquals(200, response.status());
        assertTrue(response.cacheHit());
        assertEquals("{\"message\":\"cached response\"}", response.body());
    }
}
