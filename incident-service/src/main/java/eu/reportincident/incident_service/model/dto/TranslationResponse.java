package eu.reportincident.incident_service.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResponse {
    private String originalText;
    private String translatedText;
    private String detectedLanguage;
    private String targetLanguage;
}