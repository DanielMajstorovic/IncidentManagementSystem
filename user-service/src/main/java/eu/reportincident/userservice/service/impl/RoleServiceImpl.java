package eu.reportincident.userservice.service.impl;

import eu.reportincident.userservice.exception.ResourceNotFoundException;
import eu.reportincident.userservice.model.dto.RoleDto;
import eu.reportincident.userservice.model.entity.Endpoint;
import eu.reportincident.userservice.model.entity.Role;
import eu.reportincident.userservice.model.repository.EndpointRepository;
import eu.reportincident.userservice.model.repository.RoleRepository;
import eu.reportincident.userservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final EndpointRepository endpointRepository;

    public List<RoleDto> findAllRoles() {
        return roleRepository.findAll().stream().map(RoleDto::new).collect(Collectors.toList());
    }

    @Transactional
    public RoleDto createRole(String name) {
        if (roleRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Role with name " + name + " already exists.");
        }
        Role newRole = new Role();
        newRole.setName(name.toUpperCase());
        return new RoleDto(roleRepository.save(newRole));
    }

    @Transactional
    public RoleDto updateRolePermissions(Long roleId, Set<Long> endpointIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        List<Endpoint> endpoints = endpointRepository.findAllById(endpointIds);
        role.setEndpoints(new HashSet<>(endpoints));

        return new RoleDto(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }

}