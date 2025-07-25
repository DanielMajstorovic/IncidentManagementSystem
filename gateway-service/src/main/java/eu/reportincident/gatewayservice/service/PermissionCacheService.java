package eu.reportincident.gatewayservice.service;

import eu.reportincident.gatewayservice.client.UserFeignClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class PermissionCacheService {

    private final UserFeignClient userFeignClient;
    // Koristimo ConcurrentHashMap jer mu se može pristupati iz više tredova
    private Map<String, List<String>> permissionMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initCache() {
        log.info("Initializing permission cache...");
        refreshCache();
    }

    @Scheduled(fixedRateString = "${permission.cache.refresh-rate-ms:300000}") // Osvežava na svakih 5 min
    public void refreshCache() {
        try {
            log.info("Refreshing permission cache...");
            this.permissionMap = userFeignClient.getPermissionMap();
            log.info("Permission cache refreshed successfully. Loaded {} rules.", permissionMap.size());
        } catch (Exception e) {
            log.error("Failed to refresh permission cache: {}", e.getMessage());
        }
    }

    public List<String> getRequiredRolesForPath(String path) {
        // Vraća listu rola za datu putanju iz keširane mape
        return permissionMap.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}