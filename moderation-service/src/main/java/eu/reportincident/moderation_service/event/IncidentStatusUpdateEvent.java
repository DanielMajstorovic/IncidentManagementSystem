package eu.reportincident.moderation_service.event;

import eu.reportincident.moderation_service.model.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncidentStatusUpdateEvent {
    private Long incidentId;
    private IncidentStatus status;
    private LocalDateTime timestamp;
}

