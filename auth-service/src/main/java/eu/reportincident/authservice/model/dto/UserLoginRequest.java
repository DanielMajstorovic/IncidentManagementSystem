package eu.reportincident.authservice.model.dto;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String email;
    private String name;
}