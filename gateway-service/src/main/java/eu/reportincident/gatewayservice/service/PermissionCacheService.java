package eu.reportincident.gatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PermissionCacheService implements ApplicationListener<ApplicationReadyEvent> {

    private final WebClient.Builder webClientBuilder;
    // Tip mape je sada ugnježđen
    private Map<String, Map<String, List<String>>> permissionMap = new ConcurrentHashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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
        webClientBuilder.build().get()
                .uri("http://user-service/internal/permissions")
                .retrieve()
                // Ažuriramo tip koji očekujemo od WebClient-a
                .bodyToMono(new ParameterizedTypeReference<Map<String, Map<String, List<String>>>>() {})
                .doOnSuccess(map -> {
                    this.permissionMap = new ConcurrentHashMap<>(map);
                    log.info("Permission cache refreshed successfully. Loaded rules for {} paths.", permissionMap.size());
                })
                .doOnError(error -> log.error("Failed to refresh permission cache", error))
                .subscribe();
    }

    // ===== Metoda je sada preimenovana i prima i HTTP metodu =====
    public List<String> getRequiredRoles(String path, String method) {
        // 1. Pronađi najbolji odgovarajući obrazac putanje (logika ostaje ista)
        String bestMatch = permissionMap.keySet().stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .sorted(pathMatcher.getPatternComparator(path))
                .findFirst()
                .orElse(null);

        if (bestMatch == null) {
            // Nema pravila za ovu putanju, smatramo je otvorenom za sve autentifikovane korisnike
            return null;
        }

        // 2. Iz mape, za najbolju putanju, dohvati mapu metoda i rola
        Map<String, List<String>> methodsForPath = permissionMap.get(bestMatch);
        if (methodsForPath == null) {
            return null; // Ovo se ne bi smelo desiti, ali je sigurna provera
        }

        // 3. Iz te mape, dohvati listu rola za specifičnu HTTP metodu
        // Ako ne postoji unos za datu metodu (npr. POST), vraća null
        return methodsForPath.get(method);
    }
}