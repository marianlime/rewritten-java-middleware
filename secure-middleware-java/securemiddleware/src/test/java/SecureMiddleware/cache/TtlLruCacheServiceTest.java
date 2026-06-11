package SecureMiddleware.cache;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TtlLruCacheServiceTest {

    @Test
    void shouldReturnCachedValueBeforeExpiry() {
        TtlLruCacheService service = new TtlLruCacheService();

        service.put("client-123", "cached-auth-check", 60);

        var result = service.get("client-123");

        assertTrue(result.isPresent());
        assertEquals("cached-auth-check", result.get().value());
    }

    @Test
    void shouldEvictExpiredEntries() {
        TtlLruCacheService service = new TtlLruCacheService();

        service.put("expired-key", "expired-value", 0);

        var result = service.get("expired-key");

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldEvictLeastRecentlyUsedEntryWhenCapacityExceeded() {
        TtlLruCacheService service = new TtlLruCacheService();

        for (int i = 0; i < 100; i++) {
            service.put("key-" + i, "value-" + i, 60);
        }

        service.get("key-0");
        service.put("key-100", "value-100", 60);

        assertTrue(service.get("key-0").isPresent());
        assertTrue(service.get("key-1").isEmpty());
        assertTrue(service.get("key-100").isPresent());
    }
}
