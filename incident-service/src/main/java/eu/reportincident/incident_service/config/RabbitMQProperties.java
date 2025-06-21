package eu.reportincident.incident_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "rabbitmq")
@Getter
@Setter
public class RabbitMQProperties {
    private String host;
    private Integer port;
    private String username;
    private String password;
    private String exchange;
    private String queueIncidentCreated;
    private String queueStatusUpdated;
    private String routingKeyIncidentCreated;
    private String routingKeyStatusUpdated;
}
