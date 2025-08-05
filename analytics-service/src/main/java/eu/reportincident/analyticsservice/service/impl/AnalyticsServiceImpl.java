package eu.reportincident.analyticsservice.service.impl;

import eu.reportincident.analyticsservice.client.IncidentServiceClient;
import eu.reportincident.analyticsservice.model.dto.AnalyticsResult;
import eu.reportincident.analyticsservice.model.dto.client.IncidentDetailsDto;
import eu.reportincident.analyticsservice.model.entity.IncidentAnalytics;
import eu.reportincident.analyticsservice.repository.IncidentAnalyticsRepository;
import eu.reportincident.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImpl implements AnalyticsService {

    private final IncidentAnalyticsRepository repository;
    private final IncidentServiceClient incidentServiceClient;

    @Override
    public void processIncidentCreation(Long incidentId, LocalDateTime timestamp) {
        try {
            // KORAK 1: Pozivamo incident-service da dobijemo detalje
            log.info("Fetching details for incidentId: {}", incidentId);
            IncidentDetailsDto incidentDetails = incidentServiceClient.getIncidentById(incidentId);
            log.info("Successfully fetched details for incidentId: {}", incidentId);

            // KORAK 2: Kreiramo unos u našoj bazi sa pravim podacima
            IncidentAnalytics newEntry = IncidentAnalytics.builder()
                    .incidentId(incidentId)
                    .type(incidentDetails.getType())
                    .subtype(incidentDetails.getSubtype())
                    .city(incidentDetails.getLocation() != null ? incidentDetails.getLocation().getCity() : "Unknown")
                    .status(incidentDetails.getStatus())
                    .reportedAt(timestamp)
                    .lastUpdatedAt(timestamp)
                    .build();

            repository.save(newEntry);
            log.info("Saved new analytics entry for incidentId: {}", incidentId);

        } catch (Exception e) {
            // U slučaju da incident-service nije dostupan, beležimo grešku
            // i čuvamo unos sa "placeholder" podacima da ne bismo izgubili događaj.
            log.error("Failed to fetch details for incidentId: {}. Saving with placeholder data. Error: {}", incidentId, e.getMessage());

            IncidentAnalytics fallbackEntry = IncidentAnalytics.builder()
                    .incidentId(incidentId)
                    .type("FETCH_FAILED")
                    .subtype("FETCH_FAILED")
                    .city("FETCH_FAILED")
                    .status("PENDING")
                    .reportedAt(timestamp)
                    .lastUpdatedAt(timestamp)
                    .build();
            repository.save(fallbackEntry);
        }
    }

    @Override
    public void processStatusUpdate(Long incidentId, String newStatus, LocalDateTime timestamp) {
        repository.findById(incidentId).ifPresent(entry -> {
            entry.setStatus(newStatus);
            entry.setLastUpdatedAt(timestamp);
            repository.save(entry);
        });
    }

    @Override
    public List<AnalyticsResult> getIncidentsGroupedByType() {
        return repository.countIncidentsByType();
    }

    @Override
    public List<AnalyticsResult> getIncidentsGroupedByDay() {
        return repository.countIncidentsByDay().stream()
                .map(nativeResult -> new AnalyticsResult(nativeResult.getKey(), nativeResult.getCount()))
                .collect(Collectors.toList());
    }

    @Override
    public List<AnalyticsResult> getIncidentsGroupedByCity() {
        return repository.countIncidentsByCity();
    }
}