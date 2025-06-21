package eu.reportincident.moderation_service.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncidentCreatedEvent implements Serializable {
    private Long incidentId;
    private LocalDateTime timestamp;
}

