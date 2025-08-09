package eu.reportincident.gatewayservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher; // Importujemo AntPathMatcher
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PermissionCacheService implements ApplicationListener<ApplicationReadyEvent> {

    private final WebClient.Builder webClientBuilder;
    private Map<String, List<String>> permissionMap = new ConcurrentHashMap<>();
    private final AntPathMatcher pathMatcher = new AntPathMatcher(); // Kreiramo instancu

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
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<String>>>() {})
                .doOnSuccess(map -> {
                    this.permissionMap = new ConcurrentHashMap<>(map);
                    log.info("Permission cache refreshed successfully. Loaded {} rules.", permissionMap.size());
                })
                .doOnError(error -> log.error("Failed to refresh permission cache", error))
                .subscribe();
    }

    // ===== POTPUNO NOVA, ROBUSTNA LOGIKA =====
    public List<String> getRequiredRolesForPath(String path) {
        // 1. Pronađi SVE obrasce koji se poklapaju sa dolaznom putanjom
        List<String> matchingPatterns = new java.util.ArrayList<>(permissionMap.keySet().stream()
                .filter(pattern -> pathMatcher.match(pattern, path))
                .toList());

        if (matchingPatterns.isEmpty()) {
            return null; // Nema pravila za ovu putanju, smatramo je otvorenom za autentifikovane korisnike
        }

        // 2. Sortiraj obrasce od najspecifičnijeg ka najmanje specifičnom
        // Primer: "/users/{id}" je specifičniji od "/users/**"
        matchingPatterns.sort(pathMatcher.getPatternComparator(path));

        // 3. Uzmi NAJSPECIFIČNIJI obrazac (prvi u sortiranoj listi)
        String bestMatch = matchingPatterns.get(0);
        log.debug("Best pattern match for path '{}' is '{}'", path, bestMatch);

        // 4. Vrati role koje su definisane za taj, najspecifičniji obrazac
        return permissionMap.get(bestMatch);
    }
}