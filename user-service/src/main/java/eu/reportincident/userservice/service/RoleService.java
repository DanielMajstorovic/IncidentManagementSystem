package eu.reportincident.userservice.service;

import eu.reportincident.userservice.model.dto.RoleDto;

import java.util.List;
import java.util.Set;

public interface RoleService {

    List<RoleDto> findAllRoles();
    RoleDto createRole(String name);
    RoleDto updateRolePermissions(Long roleId, Set<Long> endpointIds);
    void deleteRole(Long roleId);


}
