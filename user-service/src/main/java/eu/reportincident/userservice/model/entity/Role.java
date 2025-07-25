package eu.reportincident.userservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // "USER", "MODERATOR", "ADMIN"

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_endpoints", // Nova join tabela
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "endpoint_id")
    )
    private Set<Endpoint> endpoints; // Direktna veza sa endpointima
}