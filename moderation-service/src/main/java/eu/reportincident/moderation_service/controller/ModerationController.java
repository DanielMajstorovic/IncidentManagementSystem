package eu.reportincident.moderation_service.controller;

import eu.reportincident.moderation_service.model.dto.Incident;
import eu.reportincident.moderation_service.model.dto.IncidentModeration;
import eu.reportincident.moderation_service.model.enums.IncidentStatus;
import eu.reportincident.moderation_service.model.request.IncidentStatusUpdateRequest;
import eu.reportincident.moderation_service.service.ModerationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/moderation")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentStatus> getIncidentStatus(@PathVariable Long incidentId) {
        return ResponseEntity.ok(moderationService.getIncidentStatus(incidentId));
    }

    @PutMapping("/{incidentId}/status")
    public ResponseEntity<IncidentModeration> updateIncidentStatus(@PathVariable long incidentId, @RequestBody IncidentStatusUpdateRequest request) {
        // TODO: Pass moderatorId, after authentication part
        return ResponseEntity.ok(moderationService.updateIncidentStatus(incidentId, request.getStatus(), 1L));
    }

    @GetMapping("/incidents/{incidentId}")
    public ResponseEntity<Incident> getIncident(@PathVariable Long incidentId) {
        return ResponseEntity.ok(moderationService.getIncident(incidentId));
    }
}
