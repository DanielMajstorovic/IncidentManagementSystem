package eu.reportincident.incident_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "incident_image")
public class IncidentImageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; // Putanja slike (ili Base64 string)

    @ManyToOne
    @JoinColumn(name = "incident_id", nullable = false)
    private IncidentEntity incident;
}

