package eu.reportincident.alert_service.repository;

import eu.reportincident.alert_service.model.entity.AlertLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AlertLogRepository extends JpaRepository<AlertLog, Long> {
    List<AlertLog> findByAlertTimestampAfter(LocalDateTime timestamp);

    Optional<AlertLog> findFirstByOrderByAlertTimestampDesc();
}