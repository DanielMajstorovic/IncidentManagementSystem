package eu.reportincident.moderation_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableRabbit
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
    public RabbitTemplate rabbitTemplate(MessageConverter jacksonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory());
        template.setMessageConverter(jacksonMessageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jacksonMessageConverter);
        return factory;
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(rabbitMQProperties.getExchange());
    }

    @Bean
    public Queue incidentCreatedQueue() {
        return new Queue(rabbitMQProperties.getQueueIncidentCreated(), true);
    }

    @Bean
    public Queue statusUpdatedQueue() {
        return new Queue(rabbitMQProperties.getQueueStatusUpdated(), true);
    }

    @Bean
    public Binding incidentCreatedBinding() {
        return BindingBuilder.bind(incidentCreatedQueue())
                .to(exchange())
                .with(rabbitMQProperties.getRoutingKeyIncidentCreated());
    }

    @Bean
    public Binding statusUpdatedBinding() {
        return BindingBuilder.bind(statusUpdatedQueue())
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

