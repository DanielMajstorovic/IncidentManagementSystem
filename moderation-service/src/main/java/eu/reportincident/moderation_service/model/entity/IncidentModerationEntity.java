package eu.reportincident.moderation_service.model.entity;

import eu.reportincident.moderation_service.model.enums.IncidentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "incident_moderation")
public class IncidentModerationEntity {

    @Id
    private Long incidentId;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status;

    private LocalDateTime statusChangeTime;

    private Long moderatorId = null;
}