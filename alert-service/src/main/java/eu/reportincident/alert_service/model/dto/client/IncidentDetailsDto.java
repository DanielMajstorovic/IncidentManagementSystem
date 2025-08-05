package eu.reportincident.alert_service.model.dto.client;
import lombok.Data;
@Data
public class IncidentDetailsDto {
    private Long id;
    private LocationDto location;
    private java.time.LocalDateTime reportedAt;
}