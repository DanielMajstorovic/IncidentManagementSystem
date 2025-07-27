package eu.reportincident.incident_service.model.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class TranslationRequest {
    @NotEmpty
    private String text;
}