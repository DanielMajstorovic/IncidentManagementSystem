package eu.reportincident.alert_service.repository;

import eu.reportincident.alert_service.model.entity.AlertIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AlertIncidentRepository extends JpaRepository<AlertIncident, Long> {
    List<AlertIncident> findByReportedAtAfter(LocalDateTime timestamp);
    void deleteByReportedAtBefore(LocalDateTime timestamp);
}