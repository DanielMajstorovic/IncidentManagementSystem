package eu.reportincident.alert_service.config;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {
    @Value("${rabbitmq.exchange}") private String exchangeName;
    @Value("${rabbitmq.queue-incident-created}") private String queueName;
    @Value("${rabbitmq.routing-key-incident-created}") private String routingKey;
    @Bean
    public MessageConverter jacksonMessageConverter() { return new Jackson2JsonMessageConverter(); }
    @Bean
    public TopicExchange exchange() { return new TopicExchange(exchangeName); }
    @Bean
    public Queue incidentCreatedAlertsQueue() { return new Queue(queueName, true); }
    @Bean
    public Binding incidentCreatedAlertsBinding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}