package eu.reportincident.incident_service.model.dto;

import eu.reportincident.incident_service.model.enums.IncidentStatus;
import eu.reportincident.incident_service.model.enums.IncidentSubtype;
import eu.reportincident.incident_service.model.enums.IncidentType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incident implements Serializable {

    private Long id;
    private IncidentType type;
    private IncidentSubtype subtype;
    private Location location;
    private String description;
    private List<IncidentImage> images;
    private LocalDateTime reportedAt;
    private IncidentStatus status;
}
