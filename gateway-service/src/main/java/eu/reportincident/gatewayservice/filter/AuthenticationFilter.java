package eu.reportincident.gatewayservice.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import eu.reportincident.gatewayservice.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    // Mapa koja definiše koje role mogu pristupiti kojim putanjama
    private static final Map<String, List<String>> requiredRoles = Map.of(
            "/incident-service/api/v1/incidents/status/PENDING", List.of("MODERATOR", "ADMIN"),
            "/user-service/api/v1/users/admin", List.of("ADMIN")
            // Dodajte ovde sve ostale zaštićene putanje...
    );


    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 1. Proveravamo da li je putanja otvorena ili zaštićena
            if (routeValidator.isSecured.test(exchange.getRequest())) {

                // 2. Proveravamo da li postoji Authorization header
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.warn("Missing authorization header");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // 3. Izvlačimo token iz hedera
                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    log.warn("Authorization header is not a Bearer token");
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                String token = authHeader.substring(7);

                try {
                    // 4. Validacija tokena
                    DecodedJWT decodedJWT = jwtUtil.validateToken(token);
                    String role = jwtUtil.getClaim(decodedJWT, "role");

                    // 5. Autorizacija na osnovu role
                    String path = exchange.getRequest().getPath().toString();
                    List<String> rolesForPath = getRequiredRolesForPath(path);

                    if (rolesForPath != null && !rolesForPath.contains(role)) {
                        log.warn("User with role '{}' attempted to access protected route '{}'", role, path);
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

    private List<String> getRequiredRolesForPath(String path) {
        // Pronalazi najspecifičnije poklapanje
        return requiredRoles.entrySet().stream()
                .filter(entry -> path.startsWith(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null); // Ako putanja nije u mapi, dozvoljen je pristup (za USER rolu)
    }

    public static class Config {
        // Prazna konfiguraciona klasa
    }
}