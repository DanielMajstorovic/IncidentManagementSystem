package eu.reportincident.userservice.model.repository;

import eu.reportincident.userservice.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.endpoints WHERE r.name = :name")
    Optional<Role> findByNameWithEndpoints(@Param("name") String name);

    // Originalna metoda ostaje korisna
    Optional<Role> findByName(String name);
}