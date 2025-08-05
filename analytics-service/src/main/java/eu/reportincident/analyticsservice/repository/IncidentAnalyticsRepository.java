package eu.reportincident.analyticsservice.repository;

import eu.reportincident.analyticsservice.model.dto.AnalyticsResult;
import eu.reportincident.analyticsservice.model.entity.IncidentAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentAnalyticsRepository extends JpaRepository<IncidentAnalytics, Long> {

    // Upit za grupisanje po tipu
    @Query("SELECT new eu.reportincident.analyticsservice.model.dto.AnalyticsResult(ia.type, COUNT(ia)) FROM IncidentAnalytics ia GROUP BY ia.type")
    List<AnalyticsResult> countIncidentsByType();

    @Query(value = "SELECT DATE_FORMAT(ia.reported_at, '%Y-%m-%d') as `key`, COUNT(ia.incident_id) as `count` " +
            "FROM incident_analytics ia " +
            "GROUP BY `key` " +
            "ORDER BY `key`",
            nativeQuery = true)
    List<AnalyticsResultNative> countIncidentsByDay();

    // Upit za grupisanje po gradu
    @Query("SELECT new eu.reportincident.analyticsservice.model.dto.AnalyticsResult(ia.city, COUNT(ia)) FROM IncidentAnalytics ia GROUP BY ia.city")
    List<AnalyticsResult> countIncidentsByCity();


    interface AnalyticsResultNative {
        String getKey();
        Long getCount();
    }
}