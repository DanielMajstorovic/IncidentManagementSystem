package eu.reportincident.moderation_service.model.request;


import eu.reportincident.moderation_service.model.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentStatusUpdateRequest {
    private IncidentStatus status;
}
