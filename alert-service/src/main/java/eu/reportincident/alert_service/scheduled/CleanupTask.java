package eu.reportincident.alert_service.scheduled;

import eu.reportincident.alert_service.config.AlertProperties;
import eu.reportincident.alert_service.repository.AlertIncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Component
@Slf4j
@RequiredArgsConstructor
public class CleanupTask {
    private final AlertIncidentRepository incidentRepository;
    private final AlertProperties alertProperties;

    // Run once every day at 3 AM
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void cleanupOldIncidents() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(alertProperties.getTimeWindowDays() + 1); // +1 day buffer
        log.info("Cleaning up incidents older than {}", cutoff);
        incidentRepository.deleteByReportedAtBefore(cutoff);
        log.info("Cleanup task finished.");
    }
}