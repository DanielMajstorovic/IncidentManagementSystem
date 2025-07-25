package eu.reportincident.userservice.config;

import eu.reportincident.userservice.model.entity.Endpoint;
import eu.reportincident.userservice.model.entity.Role;
import eu.reportincident.userservice.model.entity.User;
import eu.reportincident.userservice.model.repository.EndpointRepository;
import eu.reportincident.userservice.model.repository.RoleRepository;
import eu.reportincident.userservice.model.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EndpointRepository endpointRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting database seeding process...");

        // Korak 1: Kreiraj osnovne role ako ne postoje
        Role adminRole = createRoleIfNotFound("ADMIN");
        Role moderatorRole = createRoleIfNotFound("MODERATOR");
        Role userRole = createRoleIfNotFound("USER");

        // Korak 2: Kreiraj endpointe ako ne postoje
        Endpoint viewPendingIncidents = createEndpointIfNotFound("GET", "/incident-service/api/v1/incidents/status/PENDING");
        Endpoint updateIncidents = createEndpointIfNotFound("PUT", "/incident-service/api/v1/incidents");
        Endpoint viewUsers = createEndpointIfNotFound("GET", "/user-service/api/v1/users");
        Endpoint deleteUsers = createEndpointIfNotFound("DELETE", "/user-service/api/v1/users");
        Endpoint createIncident = createEndpointIfNotFound("POST", "/incident-service/api/v1/incidents");

        // Korak 3: Poveži role sa endpointima
        // ===== ISPRAVLJENA LOGIKA POVEZIVANJA =====

        // Definišemo dozvole za usera
        Set<Endpoint> userPermissions = new HashSet<>();
        userPermissions.add(createIncident);
        linkPermissionsToRole(userRole, userPermissions);

        // Definišemo dozvole za moderatora
        Set<Endpoint> moderatorPermissions = new HashSet<>();
        moderatorPermissions.add(viewPendingIncidents);
        moderatorPermissions.add(updateIncidents);
        linkPermissionsToRole(moderatorRole, moderatorPermissions);

        // Definišemo dozvole za admina (sve što ima moderator + dodatne)
        Set<Endpoint> adminPermissions = new HashSet<>(moderatorPermissions);
        adminPermissions.add(viewUsers);
        adminPermissions.add(deleteUsers);
        linkPermissionsToRole(adminRole, adminPermissions);

        // Korak 4: Kreiraj specifične korisničke naloge ako ne postoje
        createUserIfNotFound("danielmajstorovic033@gmail.com", "Daniel Majstorovic", adminRole);
        createUserIfNotFound("sniinsuranceapp@gmail.com", "sni-insurance-app", moderatorRole);
        //createUserIfNotFound("pilkprojekat@gmail.com", "Pilk Pilk", userRole);

        log.info("Database seeding process finished.");
    }

    private void linkPermissionsToRole(Role role, Set<Endpoint> endpoints) {
        // Učitavamo rolu sa postojećim endpointima da izbegnemo detached entity problem
        Role managedRole = roleRepository.findByNameWithEndpoints(role.getName())
                .orElseThrow(() -> new IllegalStateException("Role not found: " + role.getName()));

        boolean needsUpdate = false;
        for (Endpoint endpoint : endpoints) {
            // Dodajemo samo one endpointe koje rola već nema
            if (managedRole.getEndpoints().add(endpoint)) {
                needsUpdate = true;
            }
        }

        if (needsUpdate) {
            roleRepository.save(managedRole);
            log.info("Linked {} new endpoint(s) to role: {}", endpoints.size(), role.getName());
        }
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name).orElseGet(() -> {
            log.info("Creating role: {}", name);
            Role newRole = new Role();
            newRole.setName(name);
            return roleRepository.save(newRole);
        });
    }

    private Endpoint createEndpointIfNotFound(String httpMethod, String pathPattern) {
        return endpointRepository.findByPathPattern(pathPattern).orElseGet(() -> {
            log.info("Creating endpoint: {} {}", httpMethod, pathPattern);
            Endpoint newEndpoint = new Endpoint();
            newEndpoint.setHttpMethod(httpMethod);
            newEndpoint.setPathPattern(pathPattern);
            return endpointRepository.save(newEndpoint);
        });
    }

    private void createUserIfNotFound(String email, String name, Role role) {
        if (userRepository.findByEmail(email).isEmpty()) {
            log.info("Creating user: {} with role {}", email, role.getName());
            User user = User.builder()
                    .email(email)
                    .name(name)
                    .role(role)
                    .blocked(false)
                    .deleted(false)
                    .build();
            userRepository.save(user);
        } else {
            log.info("User {} already exists.", email);
        }
    }
}