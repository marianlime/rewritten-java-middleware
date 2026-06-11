package SecureMiddleware.cache;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cache")
public class CacheController {

    private final TtlLruCacheService cacheService;

    public CacheController(TtlLruCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PostMapping
    public CacheResponse put(@RequestBody CachePutRequest request) {
        cacheService.put(request.key(), request.value(), request.ttlSeconds());

        return cacheService.get(request.key())
                .map(entry -> new CacheResponse(entry.key(), entry.value(), true, entry.expiresAtMillis()))
                .orElseThrow();
    }

    @GetMapping("/{key}")
    public CacheResponse get(@PathVariable String key) {
        return cacheService.get(key)
                .map(entry -> new CacheResponse(entry.key(), entry.value(), true, entry.expiresAtMillis()))
                .orElse(new CacheResponse(key, null, false, 0));
    }

    @GetMapping("/size")
    public int size() {
        return cacheService.size();
    }
}
