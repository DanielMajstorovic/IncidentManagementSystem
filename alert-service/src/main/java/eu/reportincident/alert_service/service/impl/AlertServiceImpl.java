package eu.reportincident.alert_service.service.impl;

import eu.reportincident.alert_service.client.IncidentServiceClient;
import eu.reportincident.alert_service.client.UserServiceClient;
import eu.reportincident.alert_service.config.AlertProperties;
import eu.reportincident.alert_service.model.dto.client.IncidentDetailsDto;
import eu.reportincident.alert_service.model.entity.AlertIncident;
import eu.reportincident.alert_service.model.entity.AlertLog;
import eu.reportincident.alert_service.repository.AlertIncidentRepository;
import eu.reportincident.alert_service.repository.AlertLogRepository;
import eu.reportincident.alert_service.service.AlertService;
import eu.reportincident.alert_service.service.NotificationService;
import eu.reportincident.alert_service.service.util.DistanceCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertServiceImpl implements AlertService {

    private final IncidentServiceClient incidentServiceClient;
    private final UserServiceClient userServiceClient;
    private final AlertIncidentRepository incidentRepository;
    private final AlertLogRepository alertLogRepository;
    private final NotificationService notificationService;
    private final DistanceCalculator distanceCalculator;
    private final AlertProperties alertProperties;

    @Override
    public void processNewIncident(Long incidentId) {
        try {
            IncidentDetailsDto newIncidentDetails = incidentServiceClient.getIncidentById(incidentId);
            AlertIncident newAlertIncident = AlertIncident.builder()
                    .incidentId(newIncidentDetails.getId())
                    .latitude(newIncidentDetails.getLocation().getLatitude())
                    .longitude(newIncidentDetails.getLocation().getLongitude())
                    .reportedAt(newIncidentDetails.getReportedAt())
                    .build();

            incidentRepository.save(newAlertIncident);
            log.info("Saved new incident locally for alert analysis. ID: {}", incidentId);

            checkForAlerts(newAlertIncident);

        } catch (Exception e) {
            log.error("Failed to process new incident for alerts. ID: {}. Reason: {}", incidentId, e.getMessage());
        }
    }

    private void checkForAlerts(AlertIncident newIncident) {
        LocalDateTime timeWindow = LocalDateTime.now().minusDays(alertProperties.getTimeWindowDays());
        List<AlertIncident> recentIncidents = incidentRepository.findByReportedAtAfter(timeWindow);

        List<AlertIncident> cluster = recentIncidents.stream()
                .filter(other -> distanceCalculator.calculateDistance(
                        newIncident.getLatitude(), newIncident.getLongitude(),
                        other.getLatitude(), other.getLongitude()
                ) <= alertProperties.getMaxRadiusMeters())
                .toList();

        if (cluster.size() >= alertProperties.getMinIncidentsThreshold()) {
            log.info("Potential alert triggered. Cluster size: {} incidents.", cluster.size());
            handlePotentialAlert(cluster);
        }
    }

    private void handlePotentialAlert(List<AlertIncident> cluster) {
        // Calculate cluster centroid
        double centroidLat = cluster.stream().mapToDouble(AlertIncident::getLatitude).average().orElse(0.0);
        double centroidLon = cluster.stream().mapToDouble(AlertIncident::getLongitude).average().orElse(0.0);

        // Check for cooldown
        LocalDateTime cooldownTime = LocalDateTime.now().minusMinutes(alertProperties.getCooldownPeriodMinutes());
        List<AlertLog> recentAlerts = alertLogRepository.findByAlertTimestampAfter(cooldownTime);

        boolean isCooledDown = recentAlerts.stream().noneMatch(log ->
                distanceCalculator.calculateDistance(centroidLat, centroidLon, log.getLatitude(), log.getLongitude())
                        <= alertProperties.getMaxRadiusMeters()
        );

        if (isCooledDown) {
            log.info("Cooldown period passed. Sending alert for cluster at ({}, {})", centroidLat, centroidLon);

            // Fetch moderators and send notification
            var moderators = userServiceClient.getAllUsers().stream()
                    .filter(user -> "MODERATOR".equals(user.getRole()))
                    .collect(Collectors.toList());
            notificationService.sendClusterAlert(moderators, cluster.size(), centroidLat, centroidLon);

            // Log the new alert
            AlertLog newAlertLog = AlertLog.builder()
                    .latitude(centroidLat)
                    .longitude(centroidLon)
                    .alertTimestamp(LocalDateTime.now())
                    .build();
            alertLogRepository.save(newAlertLog);
        } else {
            log.info("Alert suppressed due to cooldown period for cluster at ({}, {})", centroidLat, centroidLon);
        }
    }
}