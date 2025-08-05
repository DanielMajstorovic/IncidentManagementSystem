package eu.reportincident.analyticsservice.controller;

import eu.reportincident.analyticsservice.model.dto.AnalyticsResult;
import eu.reportincident.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/by-type")
    public ResponseEntity<List<AnalyticsResult>> getIncidentsByType() {
        return ResponseEntity.ok(analyticsService.getIncidentsGroupedByType());
    }

    @GetMapping("/by-day")
    public ResponseEntity<List<AnalyticsResult>> getIncidentsByDay() {
        return ResponseEntity.ok(analyticsService.getIncidentsGroupedByDay());
    }

    @GetMapping("/by-city")
    public ResponseEntity<List<AnalyticsResult>> getIncidentsByCity() {
        return ResponseEntity.ok(analyticsService.getIncidentsGroupedByCity());
    }
}