package SecureMiddleware.gateway;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/gateway")
public class GatewayController {

    private final GatewayService gatewayService;

    public GatewayController(GatewayService gatewayService) {
        this.gatewayService = gatewayService;
    }

    @GetMapping("/{path}")
    public GatewayResponse forward(
            @PathVariable String path,
            HttpServletRequest request
    ) {
        String correlationId = request.getHeader("X-Correlation-Id");

        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        return gatewayService.forwardGet(path, request.getQueryString(), correlationId);
    }
}
