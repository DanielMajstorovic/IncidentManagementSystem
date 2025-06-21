package eu.reportincident.incident_service.event;

import eu.reportincident.incident_service.model.entity.IncidentEntity;
import eu.reportincident.incident_service.repository.IncidentEntityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class RabbitMQConsumer {

    private final IncidentEntityRepository incidentRepository;

    public RabbitMQConsumer(IncidentEntityRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue-status-updated}")
    public void handleIncidentStatusUpdated(IncidentStatusUpdateEvent event) {
        log.info("[INCIDENT-UPDATE] Received: incidentId={}, status={}, timestamp={}",
                event.getIncidentId(), event.getStatus(), event.getTimestamp());

        Optional<IncidentEntity> optionalIncident = incidentRepository.findById(event.getIncidentId());

        if (optionalIncident.isPresent()) {
            IncidentEntity incident = optionalIncident.get();
            log.info("Updating incident: id={}, newStatus={}", incident.getId(), event.getStatus());

            incident.setStatus(event.getStatus());
            incident.setLastUpdated(event.getTimestamp());
            incidentRepository.save(incident);

            log.info("Incident updated successfully: id={}", incident.getId());
        } else {
            log.warn("Incident not found: id={}", event.getIncidentId());
        }
    }
}
