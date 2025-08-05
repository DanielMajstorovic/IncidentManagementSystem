package eu.reportincident.alert_service.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "alert_incidents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertIncident {
    @Id
    private Long incidentId;
    private double latitude;
    private double longitude;
    private LocalDateTime reportedAt;
}