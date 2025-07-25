package eu.reportincident.userservice.service.impl;

import eu.reportincident.userservice.exception.UserBlockedException;
import eu.reportincident.userservice.model.dto.UserLoginRequest;
import eu.reportincident.userservice.model.dto.UserLoginResponse;
import eu.reportincident.userservice.model.entity.Role;
import eu.reportincident.userservice.model.entity.User;
import eu.reportincident.userservice.model.repository.RoleRepository;
import eu.reportincident.userservice.model.repository.UserRepository;
import eu.reportincident.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserLoginResponse processUserLogin(UserLoginRequest loginRequest) {
        // Koristimo metodu koja ignoriše "obrisane" korisnike
        User user = userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())
                .orElseGet(() -> createNewUser(loginRequest));

        if (user.isBlocked()) {
            throw new UserBlockedException("User account with email " + user.getEmail() + " is blocked.");
        }

        // Vraćamo ime role kao string, što je potrebno za JWT
        return new UserLoginResponse(user.getId(), user.getRole().getName());
    }

    private User createNewUser(UserLoginRequest loginRequest) {
        // Pronađi podrazumevanu "USER" rolu iz baze.
        // Ako ne postoji, sistem je pogrešno konfigurisan i treba baciti grešku.
        Role defaultRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("FATAL: Default role USER not found in database."));

        User newUser = User.builder()
                .email(loginRequest.getEmail())
                .name(loginRequest.getName())
                .role(defaultRole)
                .blocked(false)
                .deleted(false)
                .build();
        return userRepository.save(newUser);
    }
}