package eu.reportincident.alert_service.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "alert_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertSettings {

    @Id
    private Long id; // Uvek će biti 1, jer imamo samo jedan red

    private int timeWindowDays;
    private int minIncidentsThreshold;
    private int maxRadiusMeters;
    private int cooldownPeriodMinutes;
}