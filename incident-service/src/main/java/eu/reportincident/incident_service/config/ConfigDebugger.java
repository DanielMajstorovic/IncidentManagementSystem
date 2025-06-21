package eu.reportincident.incident_service.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ConfigDebugger implements CommandLineRunner {

    private final RabbitMQProperties rabbitMQProperties;

    public ConfigDebugger(RabbitMQProperties rabbitMQProperties) {
        this.rabbitMQProperties = rabbitMQProperties;
    }

    @Override
    public void run(String... args) {
        System.out.println("Exchange: " + rabbitMQProperties.getExchange());
        System.out.println("Queue Incident Created: " + rabbitMQProperties.getQueueIncidentCreated());
        System.out.println("Queue Status Updated: " + rabbitMQProperties.getQueueStatusUpdated());
        System.out.println("Routing Key Incident Created: " + rabbitMQProperties.getRoutingKeyIncidentCreated());
        System.out.println("Routing Key Status Updated: " + rabbitMQProperties.getRoutingKeyStatusUpdated());
    }
}

