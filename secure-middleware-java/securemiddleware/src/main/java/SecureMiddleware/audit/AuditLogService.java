package SecureMiddleware.audit;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
public class AuditLogService {

    private static final int MAX_LOGS = 100;

    private final Deque<AuditLogEntry> recentLogs = new ConcurrentLinkedDeque<>();

    public void record(AuditLogEntry entry) {
        recentLogs.addFirst(entry);

        while (recentLogs.size() > MAX_LOGS) {
            recentLogs.pollLast();
        }
    }

    public List<AuditLogEntry> recent() {
        return new ArrayList<>(recentLogs);
    }
}
