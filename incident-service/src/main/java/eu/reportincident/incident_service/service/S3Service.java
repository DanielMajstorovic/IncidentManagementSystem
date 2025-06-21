package eu.reportincident.incident_service.service;

import eu.reportincident.incident_service.model.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface S3Service {
    FileUploadResponse uploadFile(MultipartFile file) throws IOException;
}

