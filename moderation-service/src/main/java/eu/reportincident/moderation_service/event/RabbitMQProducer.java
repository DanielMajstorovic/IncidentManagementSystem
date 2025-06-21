package eu.reportincident.moderation_service.event;


import eu.reportincident.moderation_service.config.RabbitMQProperties;
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

    public void sendIncidentStatusUpdatedEvent(IncidentStatusUpdateEvent event) {
        log.info("[RabbitMQ] Sending IncidentStatusUpdateEvent for Incident ID: {}", event.getIncidentId());
        try{
            rabbitTemplate.convertAndSend(exchange.getName(), rabbitMQProperties.getRoutingKeyStatusUpdated(), event);
            log.info("[RabbitMQ] IncidentStatusUpdateEvent for Incident ID: {} sent successfully", event.getIncidentId());
        }catch (AmqpException e){
            log.error("[RabbitMQ] Failed to send IncidentStatusUpdateEvent for Incident ID: {}. Error: {}", event.getIncidentId(), e.getMessage());
        }
    }
}

