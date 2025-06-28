package eu.reportincident.gatewayservice.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    // Lista putanja koje su otvorene i ne zahtevaju JWT token
    public static final List<String> openApiEndpoints = List.of(
            "/auth-service/oauth2/authorization/google",
            "/auth-service/login/oauth2/code/google",
            "/incident-service/api/v1/incidents/image-url/**",
            "/incident-service/api/v1/incidents/filter" // Pretpostavka da je filter za anonimne
    );

    // Predikat koji vraća true ako je putanja zaštićena
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> pathMatcher.match(uri, request.getURI().getPath()));
}