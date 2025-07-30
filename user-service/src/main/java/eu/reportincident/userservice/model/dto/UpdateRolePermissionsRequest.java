package eu.reportincident.userservice.model.dto;

import lombok.Data;
import java.util.Set;

@Data
public class UpdateRolePermissionsRequest {
    private Set<Long> endpointIds;
}