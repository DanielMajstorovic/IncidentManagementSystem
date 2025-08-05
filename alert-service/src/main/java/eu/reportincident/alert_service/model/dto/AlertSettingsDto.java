package eu.reportincident.alert_service.model.dto;

import eu.reportincident.alert_service.model.entity.AlertSettings;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Potreban za deserializaciju
public class AlertSettingsDto {
    private int timeWindowDays;
    private int minIncidentsThreshold;
    private int maxRadiusMeters;
    private int cooldownPeriodMinutes;

    // Helper konstruktor za laku konverziju iz entiteta
    public AlertSettingsDto(AlertSettings entity) {
        this.timeWindowDays = entity.getTimeWindowDays();
        this.minIncidentsThreshold = entity.getMinIncidentsThreshold();
        this.maxRadiusMeters = entity.getMaxRadiusMeters();
        this.cooldownPeriodMinutes = entity.getCooldownPeriodMinutes();
    }
}