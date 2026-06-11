package SecureMiddleware.ratelimit;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SlidingWindowRateLimiterService {

    private static final int LIMIT = 5;
    private static final int WINDOW_SECONDS = 60;

    private final Map<String, Deque<Long>> requestWindows = new ConcurrentHashMap<>();

    public RateLimitResponse check(String clientId) {
        long now = Instant.now().getEpochSecond();
        long windowStart = now - WINDOW_SECONDS;

        Deque<Long> timestamps = requestWindows.computeIfAbsent(clientId, key -> new ArrayDeque<>());

        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
                timestamps.pollFirst();
            }

            if (timestamps.size() >= LIMIT) {
                long oldestRequest = timestamps.peekFirst();
                long retryAfterSeconds = Math.max(1, (oldestRequest + WINDOW_SECONDS) - now);

                return new RateLimitResponse(
                        clientId,
                        false,
                        0,
                        retryAfterSeconds,
                        LIMIT,
                        WINDOW_SECONDS
                );
            }

            timestamps.addLast(now);

            return new RateLimitResponse(
                    clientId,
                    true,
                    LIMIT - timestamps.size(),
                    0,
                    LIMIT,
                    WINDOW_SECONDS
            );
        }
    }
}
