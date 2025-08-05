package eu.reportincident.analyticsservice.event;

import eu.reportincident.analyticsservice.model.dto.IncidentCreatedEvent;
import eu.reportincident.analyticsservice.model.dto.IncidentStatusUpdateEvent;
import eu.reportincident.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final AnalyticsService analyticsService;

    @RabbitListener(queues = "${rabbitmq.queue-incident-created}")
    public void handleIncidentCreated(IncidentCreatedEvent event) {
        log.info("Received IncidentCreatedEvent for incidentId: {}", event.getIncidentId());
        analyticsService.processIncidentCreation(event.getIncidentId(), event.getTimestamp());
    }

    @RabbitListener(queues = "${rabbitmq.queue-status-updated}")
    public void handleIncidentStatusUpdate(IncidentStatusUpdateEvent event) {
        log.info("Received IncidentStatusUpdateEvent for incidentId: {}", event.getIncidentId());
        analyticsService.processStatusUpdate(event.getIncidentId(), event.getStatus(), event.getTimestamp());
    }
}