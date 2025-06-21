package eu.reportincident.incident_service.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "log")
public class LogEntity {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;
    @Basic
    @Column(name = "message", nullable = false, length = 1000)
    private String message;
    @Basic
    @Column(name = "level", nullable = false, length = 50)
    private String level;
    @Basic
    @Column(name = "date_time", nullable = false)
    private Timestamp dateTime;
    @Basic
    @Column(name = "logger", nullable = false, length = 500)
    private String logger;
}