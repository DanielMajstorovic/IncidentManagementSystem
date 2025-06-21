package eu.reportincident.incident_service.model.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentImage {

    private Long id;
    private String imageUrl;
}
