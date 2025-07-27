package eu.reportincident.incident_service.service.impl;

import com.google.cloud.translate.v3.*;
import eu.reportincident.incident_service.service.TranslationService;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class TranslationServiceImpl implements TranslationService {

    private final TranslationServiceClient client;
    private final LocationName parent;

    public TranslationServiceImpl(@Value("${gcp.project-id}") String projectId) throws IOException {
        this.client = TranslationServiceClient.create();
        this.parent = LocationName.of(projectId, "global");
    }

    public String detectLanguage(String text) {
        DetectLanguageRequest request =
                DetectLanguageRequest.newBuilder()
                        .setParent(parent.toString())
                        .setMimeType("text/plain")
                        .setContent(text)
                        .build();

        DetectLanguageResponse response = client.detectLanguage(request);

        return response.getLanguagesList().stream()
                .max((l1, l2) -> Float.compare(l1.getConfidence(), l2.getConfidence()))
                .map(DetectedLanguage::getLanguageCode)
                .orElse("und");
    }

    public String translateText(String text, String targetLanguageCode) {
        TranslateTextRequest request =
                TranslateTextRequest.newBuilder()
                        .setParent(parent.toString())
                        .setMimeType("text/plain")
                        .setTargetLanguageCode(targetLanguageCode)
                        .addContents(text)
                        .build();

        TranslateTextResponse response = client.translateText(request);

        return response.getTranslationsList().stream()
                .findFirst()
                .map(Translation::getTranslatedText)
                .orElse(text);
    }

    @PreDestroy
    public void closeClient() {
        if (this.client != null) {
            this.client.close();
        }
    }
}