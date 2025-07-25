package eu.reportincident.userservice.model.dto;

import eu.reportincident.userservice.model.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponse {
    private Long userId;
    private Role role;
}