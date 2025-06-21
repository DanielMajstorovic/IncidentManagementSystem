package eu.reportincident.incident_service.service.impl;

import eu.reportincident.incident_service.model.dto.FileUploadResponse;
import eu.reportincident.incident_service.service.S3Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class S3ServiceImpl implements S3Service {

    @Value("${aws.bucketName}")
    private String bucketName;

    private final S3Client s3Client;

    public S3ServiceImpl(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public FileUploadResponse uploadFile(MultipartFile file) throws IOException {
        FileUploadResponse fileUploadResponse = new FileUploadResponse();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getOriginalFilename())
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(file.getOriginalFilename())).toExternalForm();

        fileUploadResponse.setFilePath(fileUrl);
        fileUploadResponse.setDateTime(LocalDateTime.now());

        return fileUploadResponse;
    }
}
