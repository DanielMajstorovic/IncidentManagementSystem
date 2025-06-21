package eu.reportincident.moderation_service.model.entity;


import eu.reportincident.moderation_service.model.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "incident_status_history")
public class IncidentStatusHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long incidentId;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    private LocalDateTime statusChangeTime;

    private Long moderatorId;
}