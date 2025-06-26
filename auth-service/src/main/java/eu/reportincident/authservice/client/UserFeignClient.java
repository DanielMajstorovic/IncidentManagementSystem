package eu.reportincident.authservice.client;

import eu.reportincident.authservice.model.dto.UserLoginRequest;
import eu.reportincident.authservice.model.dto.UserLoginResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserFeignClient {

    @PostMapping("/api/v1/users/login")
    UserLoginResponse handleUserLogin(@RequestBody UserLoginRequest loginRequest);
}