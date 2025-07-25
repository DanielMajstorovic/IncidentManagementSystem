package eu.reportincident.userservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
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
    @ToString.Exclude // Sprečava rekurzivni poziv u toString()
    @EqualsAndHashCode.Exclude // Sprečava rekurzivni poziv u equals/hashCode
    private Set<Endpoint> endpoints = new HashSet<>();
}