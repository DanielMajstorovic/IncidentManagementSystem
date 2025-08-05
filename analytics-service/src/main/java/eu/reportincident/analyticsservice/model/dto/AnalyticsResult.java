package eu.reportincident.analyticsservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AnalyticsResult {
    private String key; // Npr. "FIRE", "2025-07-26"
    private Long count; // Npr. 15
}