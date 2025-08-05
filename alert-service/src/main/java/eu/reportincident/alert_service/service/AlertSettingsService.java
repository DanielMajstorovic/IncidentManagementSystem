package eu.reportincident.alert_service.service;

import eu.reportincident.alert_service.model.dto.AlertSettingsDto;
import eu.reportincident.alert_service.model.dto.LastAlertInfoDto;
import eu.reportincident.alert_service.model.entity.AlertSettings;

public interface AlertSettingsService {

    AlertSettings getSettings();

    AlertSettingsDto updateSettings(AlertSettingsDto dto);

    LastAlertInfoDto getLastAlertInfo();
}
