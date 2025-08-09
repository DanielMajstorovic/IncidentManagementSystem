package eu.reportincident.userservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.reportincident.userservice.model.dto.PermissionsConfig;
import eu.reportincident.userservice.model.entity.Endpoint;
import eu.reportincident.userservice.model.entity.Role;
import eu.reportincident.userservice.model.entity.User;
import eu.reportincident.userservice.model.repository.EndpointRepository;
import eu.reportincident.userservice.model.repository.RoleRepository;
import eu.reportincident.userservice.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EndpointRepository endpointRepository;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database seeding process from permissions.json...");

        // Učitaj konfiguraciju iz JSON fajla
        PermissionsConfig config = loadPermissionsFromFile();

        // Korak 1: Kreiraj osnovne role ako ne postoje
        Map<String, Role> rolesMap = createRolesIfNotFound("ADMIN", "MODERATOR", "USER");
        log.info("Roles seeded successfully.");

        // Korak 2: Iteriraj kroz SVE endpointe definisane u JSON-u
        config.getEndpoints().forEach(endpointConfig -> {
            // Kreiraj ili pronađi endpoint u bazi
            Endpoint endpoint = createEndpointIfNotFound(endpointConfig.getMethod(), endpointConfig.getPath());

            // Za svaki endpoint, prođi kroz role koje mu mogu pristupiti
            endpointConfig.getRoles().forEach(roleName -> {
                Role role = rolesMap.get(roleName);
                if (role != null) {
                    // Dodaj ovaj endpoint u skup dozvola za datu rolu
                    if (role.getEndpoints().add(endpoint)) {
                        log.info("Linking endpoint '{} {}' to role '{}'", endpoint.getHttpMethod(), endpoint.getPathPattern(), role.getName());
                        roleRepository.save(role);
                    }
                }
            });
        });
        log.info("Endpoints and permissions linked successfully.");

        // Korak 3: Kreiraj specifične korisničke naloge
        createUserIfNotFound("danielmajstorovic033@gmail.com", "Daniel Majstorovic", rolesMap.get("ADMIN"));
        createUserIfNotFound("sniinsuranceapp@gmail.com", "sni-insurance-app", rolesMap.get("MODERATOR"));
        createUserIfNotFound("pilkprojekat@gmail.com", "Pilk Pilk", rolesMap.get("USER"));

        log.info("Database seeding process finished.");
    }

    private PermissionsConfig loadPermissionsFromFile() {
        try {
            Resource resource = resourceLoader.getResource("classpath:permissions.json");
            InputStream inputStream = resource.getInputStream();
            return objectMapper.readValue(inputStream, PermissionsConfig.class);
        } catch (Exception e) {
            log.error("Failed to read permissions.json", e);
            throw new RuntimeException("Could not load permissions.json", e);
        }
    }

    private Map<String, Role> createRolesIfNotFound(String... roleNames) {
        for (String name : roleNames) {
            if (roleRepository.findByName(name).isEmpty()) {
                log.info("Creating role: {}", name);
                Role newRole = new Role();
                newRole.setName(name);
                roleRepository.save(newRole);
            }
        }
        // Vraćamo mapu rola za lakši pristup kasnije
        return roleRepository.findAll().stream().collect(Collectors.toMap(Role::getName, r -> r));
    }

    private Endpoint createEndpointIfNotFound(String httpMethod, String pathPattern) {
        return endpointRepository.findByPathPatternAndHttpMethod(pathPattern, httpMethod).orElseGet(() -> {
            log.info("Creating endpoint: {} {}", httpMethod, pathPattern);
            Endpoint newEndpoint = new Endpoint();
            newEndpoint.setHttpMethod(httpMethod.toUpperCase());
            newEndpoint.setPathPattern(pathPattern);
            return endpointRepository.save(newEndpoint);
        });
    }

    private void createUserIfNotFound(String email, String name, Role role) {
        if (role == null) {
            log.warn("Cannot create user '{}', role is null.", email);
            return;
        }
        if (userRepository.findByEmail(email).isEmpty()) {
            log.info("Creating user: {} with role {}", email, role.getName());
            User user = User.builder()
                    .email(email).name(name).role(role)
                    .blocked(false).deleted(false).build();
            userRepository.save(user);
        } else {
            log.info("User {} already exists.", email);
        }
    }
}