package eu.reportincident.alert_service.controller;

import eu.reportincident.alert_service.model.dto.AlertSettingsDto;
import eu.reportincident.alert_service.model.dto.LastAlertInfoDto;
import eu.reportincident.alert_service.service.AlertSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertSettingsController {

    private final AlertSettingsService settingsService;

    @GetMapping("/settings")
    public ResponseEntity<AlertSettingsDto> getAlertSettings() {
        // Vraćamo DTO, ne entitet
        return ResponseEntity.ok(new AlertSettingsDto(settingsService.getSettings()));
    }

    @PutMapping("/settings")
    public ResponseEntity<AlertSettingsDto> updateAlertSettings(@RequestBody AlertSettingsDto settingsDto) {
        return ResponseEntity.ok(settingsService.updateSettings(settingsDto));
    }


    @GetMapping("/last")
    public ResponseEntity<LastAlertInfoDto> getLastAlert() {
        return ResponseEntity.ok(settingsService.getLastAlertInfo());
    }


}