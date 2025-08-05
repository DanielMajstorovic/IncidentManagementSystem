package eu.reportincident.alert_service.service.impl;
import eu.reportincident.alert_service.model.dto.client.UserDto;
import eu.reportincident.alert_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements NotificationService {

    private final JavaMailSender mailSender;

    @Override
    public void sendClusterAlert(List<UserDto> moderators, int incidentCount, double lat, double lon) {
        if (moderators.isEmpty()) {
            log.warn("No moderators found to send alert to.");
            return;
        }
        String subject = "ALERT: High Incident Activity Detected";
        String body = String.format(
                "An alert has been triggered for a high concentration of incidents.\n\n" +
                        "Details:\n" +
                        "- Number of incidents: %d\n" +
                        "- Approximate Location (Lat/Lon): %f, %f\n\n" +
                        "Please review the reported incidents in the system.\n\n" +
                        "This is an automated message from the Incident Reporting System.",
                incidentCount, lat, lon
        );
        for (UserDto moderator : moderators) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(moderator.getEmail());
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.info("Alert email sent to moderator: {}", moderator.getEmail());
            } catch (Exception e) {
                log.error("Failed to send alert email to {}: {}", moderator.getEmail(), e.getMessage());
            }
        }
    }
}