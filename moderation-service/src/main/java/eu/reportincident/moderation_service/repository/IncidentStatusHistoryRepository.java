package eu.reportincident.moderation_service.repository;

import eu.reportincident.moderation_service.model.entity.IncidentStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentStatusHistoryRepository extends JpaRepository<IncidentStatusHistoryEntity, Long> {
}
