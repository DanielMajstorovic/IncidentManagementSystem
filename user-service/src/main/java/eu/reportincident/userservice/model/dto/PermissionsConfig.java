package eu.reportincident.userservice.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionsConfig {
    // Sada imamo samo listu endpointa, bez odvojene mape za role
    private List<EndpointConfig> endpoints;
}