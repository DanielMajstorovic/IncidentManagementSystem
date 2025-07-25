package eu.reportincident.userservice.service.impl;

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

    @Transactional(readOnly = true) // Optimizacija za operacije koje samo čitaju podatke
    public Map<String, List<String>> getPermissionMap() {
        // Dohvatamo sve role sa njihovim povezanim endpointima odjednom
        List<Role> rolesWithEndpoints = roleRepository.findAll();

        // Transformišemo listu u mapu formata <Putanja, Lista_Rola>
        // Primer: <"/incidents/pending", ["MODERATOR", "ADMIN"]>
        return rolesWithEndpoints.stream()
                .flatMap(role -> role.getEndpoints().stream()
                        .map(endpoint -> Map.entry(endpoint.getPathPattern(), role.getName())))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }
}