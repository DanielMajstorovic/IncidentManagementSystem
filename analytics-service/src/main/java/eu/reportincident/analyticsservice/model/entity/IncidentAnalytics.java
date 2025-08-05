package eu.reportincident.analyticsservice.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentAnalytics {

    @Id
    private Long incidentId; // Koristimo isti ID kao originalni incident

    // Podaci o incidentu (preuzeti REST pozivom)
    private String type;
    private String subtype;
    private String city;

    // Status i vreme
    private String status;
    private LocalDateTime reportedAt;
    private LocalDateTime lastUpdatedAt;
}