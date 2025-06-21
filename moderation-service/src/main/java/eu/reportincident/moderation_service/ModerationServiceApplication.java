package eu.reportincident.moderation_service;

import eu.reportincident.moderation_service.config.RabbitMQProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RabbitMQProperties.class)
public class ModerationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ModerationServiceApplication.class, args);
	}

}
