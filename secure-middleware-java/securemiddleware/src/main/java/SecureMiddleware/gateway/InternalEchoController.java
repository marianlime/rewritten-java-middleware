package SecureMiddleware.gateway;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/internal")
public class InternalEchoController {

    @GetMapping("/echo")
    public Map<String, Object> echo(@RequestParam(defaultValue = "hello") String message) {
        return Map.of(
                "message", message,
                "service", "internal-echo-service",
                "timestamp", Instant.now().toString()
        );
    }
}
