package eu.reportincident.userservice.service.impl;

import eu.reportincident.userservice.model.entity.Endpoint;
import eu.reportincident.userservice.model.entity.Role;
import eu.reportincident.userservice.model.repository.RoleRepository;
import eu.reportincident.userservice.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public Map<String, Map<String, List<String>>> getPermissionMap() {
        List<Role> rolesWithEndpoints = roleRepository.findAll();

        // Kreiramo Map<Putanja, Map<Metoda, ListaRola>>
        return rolesWithEndpoints.stream()
                // Prolazimo kroz svaku rolu
                .flatMap(role -> role.getEndpoints().stream()
                        // Za svaki endpoint te role, pravimo "trojku": (putanja, metoda, ime role)
                        .map(endpoint -> new PermissionTuple(endpoint.getPathPattern(), endpoint.getHttpMethod(), role.getName()))
                )
                .collect(Collectors.groupingBy(
                        PermissionTuple::path, // 1. Grupišemo po putanji
                        Collectors.groupingBy(
                                PermissionTuple::method, // 2. Unutar svake putanje, grupišemo po metodi
                                Collectors.mapping(PermissionTuple::roleName, Collectors.toList()) // 3. Skupljamo imena rola u listu
                        )
                ));
    }

    // Pomoćna "record" klasa za lakše stream-ovanje
    private record PermissionTuple(String path, String method, String roleName) {}
}