package SecureMiddleware.cache;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class TtlLruCacheService {

    private static final int MAX_ENTRIES = 100;

    private final Map<String, CacheEntry> cache = new LinkedHashMap<>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CacheEntry> eldest) {
            return size() > MAX_ENTRIES;
        }
    };

    public synchronized void put(String key, String value, long ttlSeconds) {
        long now = Instant.now().toEpochMilli();
        long expiresAt = now + ttlSeconds * 1000;

        cache.put(key, new CacheEntry(
                key,
                value,
                now,
                expiresAt
        ));
    }

    public synchronized Optional<CacheEntry> get(String key) {
        long now = Instant.now().toEpochMilli();

        CacheEntry entry = cache.get(key);

        if (entry == null) {
            return Optional.empty();
        }

        if (entry.expired(now)) {
            cache.remove(key);
            return Optional.empty();
        }

        return Optional.of(entry);
    }

    public synchronized int size() {
        evictExpired();
        return cache.size();
    }

    public synchronized void evictExpired() {
        long now = Instant.now().toEpochMilli();
        cache.entrySet().removeIf(entry -> entry.getValue().expired(now));
    }
}
