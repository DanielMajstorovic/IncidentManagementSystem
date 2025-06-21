package eu.reportincident.incident_service.model.request;

import eu.reportincident.incident_service.model.enums.IncidentStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FilterRequest {

    private String incidentType;
    private String incidentSubtype;
    private String location;
    private IncidentStatus status;
    private String timeRange;  // The time range string (24h, 7d, 31d)

    // Return the start date based on the time range (24h, 7d, 31d)
    public LocalDateTime getStartDate() {
        if (timeRange != null) {
            LocalDateTime now = LocalDateTime.now();
            return switch (timeRange) {
                case "1h" -> now.minusHours(1);
                case "24h" -> now.minusHours(24);
                case "7d" -> now.minusDays(7);
                case "31d" -> now.minusDays(31);
                default -> null;
            };
        }
        return null;
    }
}
