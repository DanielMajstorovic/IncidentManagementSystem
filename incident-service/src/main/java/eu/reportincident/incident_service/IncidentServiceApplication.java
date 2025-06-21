package eu.reportincident.incident_service;

import eu.reportincident.incident_service.config.RabbitMQProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RabbitMQProperties.class)
public class IncidentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IncidentServiceApplication.class, args);
    }

}
