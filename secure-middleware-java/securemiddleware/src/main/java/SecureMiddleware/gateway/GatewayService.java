package SecureMiddleware.gateway;

import SecureMiddleware.cache.TtlLruCacheService;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;

@Service
public class GatewayService {

    private static final String INTERNAL_BASE_URL = "http://localhost:8080/api/v1/internal";
    private static final long CACHE_TTL_SECONDS = 30;

    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "connection",
            "keep-alive",
            "proxy-authenticate",
            "proxy-authorization",
            "te",
            "trailer",
            "transfer-encoding",
            "upgrade"
    );

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final TtlLruCacheService cacheService;

    public GatewayService(TtlLruCacheService cacheService) {
        this.cacheService = cacheService;
    }

    public GatewayResponse forwardGet(String path, String queryString, String correlationId) {
        String cacheKey = "GET:" + path + "?" + queryString;

        return cacheService.get(cacheKey)
                .map(entry -> new GatewayResponse(200, entry.value(), true))
                .orElseGet(() -> fetchAndCache(path, queryString, correlationId, cacheKey));
    }

    private GatewayResponse fetchAndCache(
            String path,
            String queryString,
            String correlationId,
            String cacheKey
    ) {
        try {
            String targetUrl = INTERNAL_BASE_URL + "/" + path;

            if (queryString != null && !queryString.isBlank()) {
                targetUrl += "?" + queryString;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("X-Correlation-Id", correlationId)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                cacheService.put(cacheKey, body, CACHE_TTL_SECONDS);
            }

            return new GatewayResponse(response.statusCode(), body, false);
        } catch (Exception exception) {
            return new GatewayResponse(502, "Gateway forwarding failed: " + exception.getMessage(), false);
        }
    }

    public boolean isHopByHopHeader(String headerName) {
        return HOP_BY_HOP_HEADERS.contains(headerName.toLowerCase());
    }
}
