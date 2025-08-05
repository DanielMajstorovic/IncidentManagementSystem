package eu.reportincident.alert_service.repository;

import eu.reportincident.alert_service.model.entity.AlertSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlertSettingsRepository extends JpaRepository<AlertSettings, Long> {
    // Pouzdan način da se uvek dobije prvi (i jedini) red
    Optional<AlertSettings> findFirstByOrderByIdAsc();
}