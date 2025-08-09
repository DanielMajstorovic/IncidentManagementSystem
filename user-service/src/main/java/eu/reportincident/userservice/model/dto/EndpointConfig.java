package eu.reportincident.userservice.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class EndpointConfig {
    private String method;
    private String path;
    private List<String> roles; // Sada svaka definicija endpointa sadrži listu rola
}