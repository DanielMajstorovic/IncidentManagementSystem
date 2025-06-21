package eu.reportincident.moderation_service.event;


import eu.reportincident.moderation_service.model.entity.IncidentModerationEntity;
import eu.reportincident.moderation_service.model.entity.IncidentStatusHistoryEntity;
import eu.reportincident.moderation_service.model.enums.IncidentStatus;
import eu.reportincident.moderation_service.repository.IncidentModerationRepository;
import eu.reportincident.moderation_service.repository.IncidentStatusHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class RabbitMQConsumer {

    private final RabbitMQProducer rabbitMQProducer;
    private final IncidentModerationRepository incidentModerationRepository;
    private final IncidentStatusHistoryRepository incidentStatusHistoryRepository;
    private final ModelMapper modelMapper;

    public RabbitMQConsumer(RabbitMQProducer rabbitMQProducer, IncidentModerationRepository incidentModerationRepository, IncidentStatusHistoryRepository incidentStatusHistoryRepository, ModelMapper modelMapper) {
        this.rabbitMQProducer = rabbitMQProducer;
        this.incidentModerationRepository = incidentModerationRepository;
        this.incidentStatusHistoryRepository = incidentStatusHistoryRepository;
        this.modelMapper = modelMapper;
    }

    @RabbitListener(queues = "${rabbitmq.queue-incident-created}")
    @Transactional
    public void handleIncidentCreated(IncidentCreatedEvent event) {

        log.info("[INCIDENT-CREATED] Received: incidentId={}, timestamp={}",
                event.getIncidentId(), event.getTimestamp());

        IncidentModerationEntity moderation = IncidentModerationEntity.builder()
                .incidentId(event.getIncidentId())
                .status(IncidentStatus.PENDING)
                .statusChangeTime(event.getTimestamp())
                .moderatorId(1L)
                .build();
        moderation.setIncidentId(event.getIncidentId());
        incidentModerationRepository.save(moderation);

        IncidentStatusHistoryEntity statusHistory = IncidentStatusHistoryEntity.builder()
                .incidentId(event.getIncidentId())
                .status(IncidentStatus.PENDING)
                .statusChangeTime(event.getTimestamp())
                .moderatorId(1L)
                .build();
        incidentStatusHistoryRepository.save(statusHistory);

        IncidentStatusUpdateEvent statusEvent = new IncidentStatusUpdateEvent(
                event.getIncidentId(),
                IncidentStatus.PENDING,
                LocalDateTime.now()
        );
        rabbitMQProducer.sendIncidentStatusUpdatedEvent(statusEvent);
    }

}

