package eu.reportincident.analyticsservice.model.dto.client;

import lombok.Data;

@Data
public class IncidentDetailsDto {
    private String type;
    private String subtype;
    private String status;
    private LocationDto location;
}