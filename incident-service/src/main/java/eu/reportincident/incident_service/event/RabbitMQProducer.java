package eu.reportincident.incident_service.event;

import eu.reportincident.incident_service.config.RabbitMQProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RabbitMQProducer {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;
    private final TopicExchange exchange;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate, RabbitMQProperties rabbitMQProperties, TopicExchange exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMQProperties = rabbitMQProperties;
        this.exchange = exchange;
    }

    public void sendIncidentCreatedEvent(IncidentCreatedEvent event) {
        log.info("[RabbitMQ] Sending IncidentCreatedEvent for Incident ID: {}", event.getIncidentId());
        try {
            rabbitTemplate.convertAndSend(exchange.getName(), rabbitMQProperties.getRoutingKeyIncidentCreated(), event);
            log.info("[RabbitMQ] IncidentCreatedEvent for Incident ID: {} sent successfully", event.getIncidentId());
        } catch (AmqpException e) {
            log.error("[RabbitMQ] Failed to send IncidentCreatedEvent for Incident ID: {}. Error: {}", event.getIncidentId(), e.getMessage());
            // TODO: Talk about mechanisms to retry sending failed messages, and how to not lose them
        }

    }
}
