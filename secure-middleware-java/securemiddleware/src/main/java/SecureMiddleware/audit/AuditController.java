package SecureMiddleware.audit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/api/v1/audit/recent")
    public List<AuditLogEntry> recentLogs() {
        return auditLogService.recent();
    }
}
