package eu.reportincident.alert_service.client;
import eu.reportincident.alert_service.model.dto.client.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@FeignClient(name = "user-service")
public interface UserServiceClient {
    @GetMapping("/api/v1/users")
    List<UserDto> getAllUsers();
}