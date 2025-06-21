package eu.reportincident.incident_service.model.request;

import eu.reportincident.incident_service.model.enums.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentStatusUpdateRequest {
    IncidentStatus status;
}
