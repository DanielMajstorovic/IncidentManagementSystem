package eu.reportincident.analyticsservice.model.dto;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class IncidentStatusUpdateEvent implements Serializable {
    private Long incidentId;
    private String status; // Koristimo String da budemo fleksibilni
    private LocalDateTime timestamp;
}