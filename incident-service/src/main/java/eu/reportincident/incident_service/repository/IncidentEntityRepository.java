package eu.reportincident.incident_service.repository;

import eu.reportincident.incident_service.model.entity.IncidentEntity;
import eu.reportincident.incident_service.model.enums.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IncidentEntityRepository extends JpaRepository<IncidentEntity, Long> {
    Page<IncidentEntity> findByStatus(IncidentStatus status, Pageable pageable);
    Optional<IncidentEntity> findByIdAndStatus(Long id, IncidentStatus status);
}
