package eu.reportincident.alert_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LastAlertInfoDto {
    private LocalDateTime lastAlertTimestamp;
}