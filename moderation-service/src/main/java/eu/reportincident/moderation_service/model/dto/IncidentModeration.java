package eu.reportincident.moderation_service.model.dto;

import eu.reportincident.moderation_service.model.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentModeration implements Serializable {
    private Long incidentId;

    private IncidentStatus status;

    private LocalDateTime statusChangeTime;

    private Long moderatorId;
}
