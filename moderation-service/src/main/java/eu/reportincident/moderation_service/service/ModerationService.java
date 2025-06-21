package eu.reportincident.moderation_service.service;


import eu.reportincident.moderation_service.model.dto.Incident;
import eu.reportincident.moderation_service.model.dto.IncidentModeration;
import eu.reportincident.moderation_service.model.enums.IncidentStatus;

public interface ModerationService {

    IncidentStatus getIncidentStatus(Long incidentId);

    IncidentModeration updateIncidentStatus(Long incidentId, IncidentStatus status, Long moderatorId);

    Incident getIncident(Long incidentId);
}
