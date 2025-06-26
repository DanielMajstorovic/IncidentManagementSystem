package eu.reportincident.authservice.model.dto;

import eu.reportincident.authservice.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponse {
    private Long userId;
    private Role role;
}