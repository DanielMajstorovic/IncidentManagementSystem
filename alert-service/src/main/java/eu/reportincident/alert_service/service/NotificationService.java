package eu.reportincident.alert_service.service;
import eu.reportincident.alert_service.model.dto.client.UserDto;
import java.util.List;
public interface NotificationService {
    void sendClusterAlert(List<UserDto> moderators, int incidentCount, double lat, double lon);
}