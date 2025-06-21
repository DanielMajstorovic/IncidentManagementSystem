package eu.reportincident.incident_service.service;

import eu.reportincident.incident_service.model.dto.Incident;
import eu.reportincident.incident_service.model.enums.IncidentStatus;
import eu.reportincident.incident_service.model.request.FilterRequest;
import eu.reportincident.incident_service.model.request.IncidentRequest;
import eu.reportincident.incident_service.model.request.IncidentStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncidentService {

    Incident findById(long id);

    Incident create(IncidentRequest incidentRequest);

    Page<Incident> filter(Pageable page, FilterRequest filterRequest);

    Page<Incident> findAll(Pageable page);

    Page<Incident> findByStatus(Pageable page, IncidentStatus status);

    Incident updateStatus(long id, IncidentStatusUpdateRequest status);
}
