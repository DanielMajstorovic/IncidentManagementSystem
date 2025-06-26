package eu.reportincident.userservice.service.impl;

import eu.reportincident.userservice.exception.UserBlockedException;
import eu.reportincident.userservice.model.dto.UserLoginRequest;
import eu.reportincident.userservice.model.dto.UserLoginResponse;
import eu.reportincident.userservice.model.entity.User;
import eu.reportincident.userservice.model.enums.Role;
import eu.reportincident.userservice.model.repository.UserRepository;
import eu.reportincident.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserLoginResponse processUserLogin(UserLoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseGet(() -> createNewUser(loginRequest));

        if (user.isBlocked()) {
            throw new UserBlockedException("User account with email " + user.getEmail() + " is blocked.");
        }

        if (user.isDeleted()) {
            throw new UserBlockedException("User account with email " + user.getEmail() + " is deleted.");
        }

        return new UserLoginResponse(user.getId(), user.getRole());
    }

    private User createNewUser(UserLoginRequest loginRequest) {
        User newUser = User.builder()
                .email(loginRequest.getEmail())
                .name(loginRequest.getName())
                .role(Role.USER)
                .blocked(false)
                .deleted(false)
                .build();
        return userRepository.save(newUser);
    }
}