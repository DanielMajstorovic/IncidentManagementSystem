package eu.reportincident.analyticsservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private final RabbitMQProperties rabbitMQProperties;

    public RabbitMQConfig(RabbitMQProperties rabbitMQProperties) {
        this.rabbitMQProperties = rabbitMQProperties;
    }


    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue incidentCreatedAnalyticsQueue() {
        // Čita ime "incident-created-queue-for-analytics" iz .yml fajla
        return new Queue(rabbitMQProperties.getQueueIncidentCreated(), true);
    }


    @Bean
    public Queue statusUpdatedAnalyticsQueue() {
        // Čita ime "incident-created-queue-for-analytics" iz .yml fajla
        return new Queue(rabbitMQProperties.getQueueStatusUpdated(), true);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(rabbitMQProperties.getExchange());
    }




    @Bean
    public Binding incidentCreatedBinding() {
        return BindingBuilder.bind(incidentCreatedAnalyticsQueue())
                .to(exchange())
                .with(rabbitMQProperties.getRoutingKeyIncidentCreated());
    }

    @Bean
    public Binding statusUpdatedBinding() {
        return BindingBuilder.bind(statusUpdatedAnalyticsQueue())
                .to(exchange())
                .with(rabbitMQProperties.getRoutingKeyStatusUpdated());
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(rabbitMQProperties.getHost());
        connectionFactory.setPort(rabbitMQProperties.getPort());
        connectionFactory.setUsername(rabbitMQProperties.getUsername());
        connectionFactory.setPassword(rabbitMQProperties.getPassword());
        return connectionFactory;
    }


}