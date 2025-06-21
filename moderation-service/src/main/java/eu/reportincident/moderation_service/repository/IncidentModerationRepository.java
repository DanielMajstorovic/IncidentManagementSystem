package eu.reportincident.moderation_service.repository;

import eu.reportincident.moderation_service.model.entity.IncidentModerationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentModerationRepository extends JpaRepository<IncidentModerationEntity, Long> {
    Optional<IncidentModerationEntity> findByIncidentId(Long incidentId);
}
