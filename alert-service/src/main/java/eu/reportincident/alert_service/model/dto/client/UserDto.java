package eu.reportincident.alert_service.model.dto.client;
import lombok.Data;
@Data
public class UserDto {
    private String email;
    private String name;
    private String role;
}