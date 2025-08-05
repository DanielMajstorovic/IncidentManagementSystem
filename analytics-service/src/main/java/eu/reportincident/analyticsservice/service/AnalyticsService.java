package eu.reportincident.analyticsservice.service;

import eu.reportincident.analyticsservice.model.dto.AnalyticsResult;

import java.time.LocalDateTime;
import java.util.List;

public interface AnalyticsService {
    void processIncidentCreation(Long incidentId, LocalDateTime timestamp);
    void processStatusUpdate(Long incidentId, String newStatus, LocalDateTime timestamp);

    List<AnalyticsResult> getIncidentsGroupedByType();
    List<AnalyticsResult> getIncidentsGroupedByDay();
    List<AnalyticsResult> getIncidentsGroupedByCity();
}