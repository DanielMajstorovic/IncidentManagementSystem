package eu.reportincident.incident_service.model.request;

import eu.reportincident.incident_service.model.dto.IncidentImage;
import eu.reportincident.incident_service.model.dto.Location;
import eu.reportincident.incident_service.model.enums.IncidentSubtype;
import eu.reportincident.incident_service.model.enums.IncidentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {
    private IncidentType type;
    private IncidentSubtype subtype;
    private Location location;
    private String description;
    private List<IncidentImage> images;
}
