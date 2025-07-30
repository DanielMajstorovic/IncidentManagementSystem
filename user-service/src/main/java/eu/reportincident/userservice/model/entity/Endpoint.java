package eu.reportincident.userservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "endpoints", uniqueConstraints = @UniqueConstraint(columnNames = {"pathPattern", "httpMethod"}))
public class Endpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String httpMethod; // "GET", "POST", etc.

    @Column(nullable = false)
    private String pathPattern; // e.g., "/incident-service/api/v1/incidents/status/PENDING"
}