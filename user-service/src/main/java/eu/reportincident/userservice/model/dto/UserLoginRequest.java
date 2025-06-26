package eu.reportincident.userservice.model.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String email;
    private String name;
}