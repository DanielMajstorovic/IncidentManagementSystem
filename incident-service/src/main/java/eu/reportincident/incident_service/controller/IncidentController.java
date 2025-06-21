package eu.reportincident.incident_service.controller;

import eu.reportincident.incident_service.model.dto.FileUploadResponse;
import eu.reportincident.incident_service.model.dto.Incident;
import eu.reportincident.incident_service.model.enums.IncidentStatus;
import eu.reportincident.incident_service.model.request.FilterRequest;
import eu.reportincident.incident_service.model.request.IncidentRequest;
import eu.reportincident.incident_service.model.request.IncidentStatusUpdateRequest;
import eu.reportincident.incident_service.service.IncidentService;
import eu.reportincident.incident_service.service.S3Service;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/incidents")
public class IncidentController {

    private final IncidentService incidentService;
    private final S3Service s3Service;

    @Autowired
    public IncidentController(IncidentService incidentService, S3Service s3Service) {
        this.incidentService = incidentService;
        this.s3Service = s3Service;
    }

    @GetMapping
    public Page<Incident> findByStatus(Pageable page, @RequestParam(value = "status", required = false) IncidentStatus status) {
        if (status == null) {
            return incidentService.findAll(page);
        } else {
            return incidentService.findByStatus(page, status);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> findById(@PathVariable long id) {
        Optional<Incident> incident = Optional.ofNullable(incidentService.findById(id));
        return incident.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @PostMapping
    public ResponseEntity<Incident> create(@RequestBody @Valid IncidentRequest incidentRequest) {
        Incident incident = incidentService.create(incidentRequest);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(incident.getId())
                .toUri();

        return ResponseEntity.created(location).body(incident);
    }

    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("file") MultipartFile[] files) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileUploadResponse response = s3Service.uploadFile(file);
                fileUrls.add(response.getFilePath());
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList("Error uploading file"));
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(fileUrls);
    }


    @PostMapping("/filter")
    public Page<Incident> findByFilter(Pageable page, @RequestBody @Valid FilterRequest filterRequest) {
        return incidentService.filter(page, filterRequest);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Incident> updateStatus(@PathVariable long id, @RequestBody IncidentStatusUpdateRequest status) {
        Incident updatedIncident = incidentService.updateStatus(id, status);
        return ResponseEntity.ok(updatedIncident);
    }

}
