package eu.reportincident.userservice.service;

import eu.reportincident.userservice.model.dto.UserLoginRequest;
import eu.reportincident.userservice.model.dto.UserLoginResponse;
import eu.reportincident.userservice.model.entity.User;

public interface UserService {

    UserLoginResponse processUserLogin(UserLoginRequest loginRequest);
}
