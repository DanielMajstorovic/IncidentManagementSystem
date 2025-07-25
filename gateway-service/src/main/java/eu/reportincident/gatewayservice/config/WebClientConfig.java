package eu.reportincident.gatewayservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    // Pita eureku valjda za ip adresu...

    @Bean
    @LoadBalanced // NAJVAŽNIJA ANOTACIJA!
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}