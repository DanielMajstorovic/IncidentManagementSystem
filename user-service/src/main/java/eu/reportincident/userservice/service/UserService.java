package eu.reportincident.userservice.service;

import eu.reportincident.userservice.model.dto.UserDto;
import eu.reportincident.userservice.model.dto.UserLoginRequest;
import eu.reportincident.userservice.model.dto.UserLoginResponse;
import eu.reportincident.userservice.model.entity.User;

import java.util.List;

public interface UserService {

    UserLoginResponse processUserLogin(UserLoginRequest loginRequest);

    List<UserDto> findAllUsers();
    UserDto updateUserRole(Long userId, String roleName);
    UserDto blockUser(Long userId);
    UserDto unblockUser(Long userId);
    void deleteUser(Long userId);
}
