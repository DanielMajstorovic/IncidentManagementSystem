package eu.reportincident.userservice.controller;

import eu.reportincident.userservice.model.dto.UpdateUserRoleRequest;
import eu.reportincident.userservice.model.dto.UserDto;
import eu.reportincident.userservice.model.dto.UserLoginRequest;
import eu.reportincident.userservice.model.dto.UserLoginResponse;
import eu.reportincident.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> handleUserLogin(@RequestBody UserLoginRequest loginRequest) {
        UserLoginResponse response = userService.processUserLogin(loginRequest);
        return ResponseEntity.ok(response);
    }


    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @PutMapping("/{id}/role")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(id, request.getRoleName()));
    }

    @PutMapping("/{id}/block")
    public ResponseEntity<UserDto> blockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.blockUser(id));
    }

    @PutMapping("/{id}/unblock")
    public ResponseEntity<UserDto> unblockUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.unblockUser(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

}