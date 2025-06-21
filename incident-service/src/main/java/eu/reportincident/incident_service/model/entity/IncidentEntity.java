package eu.reportincident.incident_service.model.entity;

import eu.reportincident.incident_service.model.dto.Location;
import eu.reportincident.incident_service.model.enums.IncidentStatus;
import eu.reportincident.incident_service.model.enums.IncidentSubtype;
import eu.reportincident.incident_service.model.enums.IncidentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "incident")
public class IncidentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private IncidentSubtype subtype;

    @Embedded
    private Location location;

    @Column(nullable = false, length = 1000)
    private String description;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<IncidentImageEntity> images = new ArrayList<>();

    private LocalDateTime reportedAt;

    private LocalDateTime lastUpdated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.REPORTED;

    @PrePersist
    protected void onCreate() {
        reportedAt = LocalDateTime.now();
        status = IncidentStatus.REPORTED; // Default status pri kreiranju incidenta
    }
}
