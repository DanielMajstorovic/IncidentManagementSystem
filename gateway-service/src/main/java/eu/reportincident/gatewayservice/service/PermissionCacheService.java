package eu.reportincident.gatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PermissionCacheService implements ApplicationListener<ApplicationReadyEvent> {

    private final WebClient.Builder webClientBuilder;
    private Map<String, List<String>> permissionMap = new ConcurrentHashMap<>();

    // Injektujemo WebClient.Builder umesto Feign klijenta
    public PermissionCacheService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("Application ready. Initializing permission cache...");
        refreshCache();
    }

    @Scheduled(fixedRateString = "${permission.cache.refresh-rate-ms:6000}")
    public void refreshCache() {
        log.info("Refreshing permission cache...");

        // Koristimo WebClient za reaktivni, neblokirajući poziv
        webClientBuilder.build().get()
                .uri("http://user-service/internal/permissions") // Koristimo ime servisa, Eureka će ga pronaći
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<String>>>() {})
                .doOnSuccess(map -> {
                    this.permissionMap = new ConcurrentHashMap<>(map); // Zamenjujemo keširanu mapu
                    log.info("Permission cache refreshed successfully. Loaded {} rules.", permissionMap.size());
                })
                .doOnError(error -> log.error("Failed to refresh permission cache", error))
                .subscribe(); // subscribe() pokreće ceo proces
    }

    public List<String> getRequiredRolesForPath(String path) {
        // Logika ostaje ista
        return permissionMap.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}