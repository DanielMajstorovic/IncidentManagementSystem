package eu.reportincident.alert_service.model.dto;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
@Data
public class IncidentCreatedEvent implements Serializable {
    private Long incidentId;
    private LocalDateTime timestamp;
}