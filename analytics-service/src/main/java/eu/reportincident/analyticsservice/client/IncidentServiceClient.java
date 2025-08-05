package eu.reportincident.analyticsservice.client;

import eu.reportincident.analyticsservice.model.dto.client.IncidentDetailsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// "incident-service" je ime pod kojim je servis registrovan na Eureku
@FeignClient(name = "incident-service")
public interface IncidentServiceClient {

    // Potpis metode mora TAČNO odgovarati onom u IncidentController-u
    @GetMapping("/api/v1/incidents/{id}")
    IncidentDetailsDto getIncidentById(@PathVariable("id") Long incidentId);
}