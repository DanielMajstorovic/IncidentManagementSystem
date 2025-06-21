package eu.reportincident.incident_service.repository;

import eu.reportincident.incident_service.model.entity.IncidentImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IncidentImageEntityRepository extends JpaRepository<IncidentImageEntity, Long> {
}