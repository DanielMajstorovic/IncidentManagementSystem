package eu.reportincident.alert_service.service.impl;

import eu.reportincident.alert_service.config.AlertProperties;
import eu.reportincident.alert_service.model.dto.AlertSettingsDto;
import eu.reportincident.alert_service.model.dto.LastAlertInfoDto;
import eu.reportincident.alert_service.model.entity.AlertSettings;
import eu.reportincident.alert_service.repository.AlertLogRepository;
import eu.reportincident.alert_service.repository.AlertSettingsRepository;
import eu.reportincident.alert_service.service.AlertSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlertSettingsServiceImpl implements AlertSettingsService {

    private final AlertSettingsRepository settingsRepository;
    private final AlertProperties defaultProperties;
    private final AlertLogRepository alertLogRepository;

    @Override
    @Transactional
    public AlertSettings getSettings() {
        return settingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(this::createDefaultSettings);
    }


    @Transactional(readOnly = true)
    public LastAlertInfoDto getLastAlertInfo() {
        return alertLogRepository.findFirstByOrderByAlertTimestampDesc()
                .map(alertLog -> new LastAlertInfoDto(alertLog.getAlertTimestamp()))
                .orElse(new LastAlertInfoDto(null));
    }



    @Override
    @Transactional
    public AlertSettingsDto updateSettings(AlertSettingsDto dto) {
        AlertSettings settings = getSettings();

        settings.setTimeWindowDays(dto.getTimeWindowDays());
        settings.setMinIncidentsThreshold(dto.getMinIncidentsThreshold());
        settings.setMaxRadiusMeters(dto.getMaxRadiusMeters());
        settings.setCooldownPeriodMinutes(dto.getCooldownPeriodMinutes());

        return new AlertSettingsDto(settingsRepository.save(settings));
    }

    private AlertSettings createDefaultSettings() {
        AlertSettings defaultSettings = AlertSettings.builder()
                .id(1L)
                .timeWindowDays(defaultProperties.getTimeWindowDays())
                .minIncidentsThreshold(defaultProperties.getMinIncidentsThreshold())
                .maxRadiusMeters(defaultProperties.getMaxRadiusMeters())
                .cooldownPeriodMinutes(defaultProperties.getCooldownPeriodMinutes())
                .build();
        return settingsRepository.save(defaultSettings);
    }
}
