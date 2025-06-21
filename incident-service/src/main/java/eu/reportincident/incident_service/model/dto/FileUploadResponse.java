package eu.reportincident.incident_service.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileUploadResponse {
    private String filePath;
    private LocalDateTime dateTime;
}
