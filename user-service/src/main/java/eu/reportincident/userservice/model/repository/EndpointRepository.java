package eu.reportincident.userservice.model.repository;

import eu.reportincident.userservice.model.entity.Endpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface EndpointRepository extends JpaRepository<Endpoint, Long> {

    // Efikasan upit koji dohvata sve endpointe i role koje im mogu pristupiti
    @Query("SELECT DISTINCT e FROM Role r JOIN r.endpoints e")
    List<Endpoint> findAllWithRoles();

    Optional<Endpoint> findByPathPatternAndHttpMethod(String pathPattern, String httpMethod);
}