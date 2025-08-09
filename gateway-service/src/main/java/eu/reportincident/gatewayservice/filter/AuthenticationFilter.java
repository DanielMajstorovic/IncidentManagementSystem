package eu.reportincident.gatewayservice.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import eu.reportincident.gatewayservice.service.PermissionCacheService;
import eu.reportincident.gatewayservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;
    private final PermissionCacheService permissionCacheService;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator, @Lazy PermissionCacheService permissionCacheService) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (routeValidator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Missing authorization header for secured route: {}", exchange.getRequest().getPath());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Authorization header is not a Bearer token");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String token = authHeader.substring(7);
                try {
                    DecodedJWT decodedJWT = jwtUtil.validateToken(token);
                    String role = jwtUtil.getClaim(decodedJWT, "role");
                    String path = exchange.getRequest().getPath().toString();
                    String method = exchange.getRequest().getMethod().name();

                    List<String> requiredRoles = permissionCacheService.getRequiredRoles(path, method);

                    if (requiredRoles == null || !requiredRoles.contains(role)) {
                        log.warn("Authorization failed. User with role '{}' attempted to access protected route '{}'", role, path);
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }
                } catch (JWTVerificationException e) {
                    log.error("Invalid JWT token: {}", e.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {}
}