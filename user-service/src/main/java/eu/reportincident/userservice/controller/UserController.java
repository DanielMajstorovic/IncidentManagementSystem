package eu.reportincident.userservice.controller;

import eu.reportincident.userservice.model.dto.UserLoginRequest;
import eu.reportincident.userservice.model.dto.UserLoginResponse;
import eu.reportincident.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}