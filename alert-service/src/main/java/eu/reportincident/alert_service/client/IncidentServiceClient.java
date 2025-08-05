package eu.reportincident.alert_service.client;
import eu.reportincident.alert_service.model.dto.client.IncidentDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "incident-service")
public interface IncidentServiceClient {
    @GetMapping("/api/v1/incidents/{id}")
    IncidentDetailsDto getIncidentById(@PathVariable("id") Long incidentId);
}