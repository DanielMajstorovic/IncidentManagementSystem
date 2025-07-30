package eu.reportincident.userservice.model.dto;

import lombok.Data;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class RoleDto {
    private Long id;
    private String name;
    private Set<Long> endpointIds; // Vraćamo samo ID-jeve endpointa

    public RoleDto(eu.reportincident.userservice.model.entity.Role role) {
        this.id = role.getId();
        this.name = role.getName();
        this.endpointIds = role.getEndpoints().stream()
                .map(eu.reportincident.userservice.model.entity.Endpoint::getId)
                .collect(Collectors.toSet());
    }
}