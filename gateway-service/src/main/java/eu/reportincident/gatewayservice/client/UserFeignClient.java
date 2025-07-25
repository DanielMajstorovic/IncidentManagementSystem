package eu.reportincident.gatewayservice.client;

import eu.reportincident.gatewayservice.dto.PermissionMapDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/internal/permissions")
    PermissionMapDto getPermissionMap();
}