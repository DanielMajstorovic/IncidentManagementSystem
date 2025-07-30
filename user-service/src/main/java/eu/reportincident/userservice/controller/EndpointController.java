package eu.reportincident.userservice.controller;

import eu.reportincident.userservice.model.entity.Endpoint;
import eu.reportincident.userservice.model.repository.EndpointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/endpoints")
@RequiredArgsConstructor
public class EndpointController {

    private final EndpointRepository endpointRepository;

    @GetMapping
    public ResponseEntity<List<Endpoint>> getAllEndpoints() {
        return ResponseEntity.ok(endpointRepository.findAll());
    }
}