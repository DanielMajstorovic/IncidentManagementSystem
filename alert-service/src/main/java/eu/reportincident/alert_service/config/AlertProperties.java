package eu.reportincident.alert_service.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Component
@ConfigurationProperties(prefix = "alerts")
@Data
public class AlertProperties {
    private int timeWindowDays;
    private int minIncidentsThreshold;
    private int maxRadiusMeters;
    private int cooldownPeriodMinutes;
}