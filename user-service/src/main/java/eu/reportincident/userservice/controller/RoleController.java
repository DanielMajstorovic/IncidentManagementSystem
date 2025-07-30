package eu.reportincident.userservice.controller;

import eu.reportincident.userservice.model.dto.CreateRoleRequest;
import eu.reportincident.userservice.model.dto.RoleDto;
import eu.reportincident.userservice.model.dto.UpdateRolePermissionsRequest;
import eu.reportincident.userservice.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAllRoles());
    }

    @PostMapping
    public ResponseEntity<RoleDto> createRole(@RequestBody CreateRoleRequest request) {
        return new ResponseEntity<>(roleService.createRole(request.getName()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<RoleDto> updateRolePermissions(@PathVariable Long id, @RequestBody UpdateRolePermissionsRequest request) {
        return ResponseEntity.ok(roleService.updateRolePermissions(id, request.getEndpointIds()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
    }
}