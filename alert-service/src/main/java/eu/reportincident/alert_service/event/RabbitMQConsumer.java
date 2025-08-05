package eu.reportincident.alert_service.event;

import eu.reportincident.alert_service.model.dto.IncidentCreatedEvent;
import eu.reportincident.alert_service.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {
    private final AlertService alertService;
    @RabbitListener(queues = "${rabbitmq.queue-incident-created}")
    public void handleIncidentCreated(IncidentCreatedEvent event) {
        log.info("Received IncidentCreatedEvent for incidentId: {}", event.getIncidentId());
        alertService.processNewIncident(event.getIncidentId());
    }
}