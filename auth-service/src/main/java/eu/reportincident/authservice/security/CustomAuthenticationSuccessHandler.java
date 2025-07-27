package eu.reportincident.authservice.security;

import eu.reportincident.authservice.client.UserFeignClient;
import eu.reportincident.authservice.model.dto.UserLoginRequest;
import eu.reportincident.authservice.model.dto.UserLoginResponse;
import eu.reportincident.authservice.util.JwtUtil;
import feign.FeignException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final UserFeignClient userFeignClient;
    private final JwtUtil jwtUtil;
    @Value("${app.oauth2.allowed-domain}")
    private String allowedDomain;
    @Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        if (email == null || !email.endsWith("@" + allowedDomain)) {
            // Umesto greške, preusmeravamo na frontend sa porukom
            String errorRedirect = UriComponentsBuilder.fromUriString(redirectUrl.replace("login-success", "login-error"))
                    .queryParam("error", "invalid_domain")
                    .build().toUriString();
            response.sendRedirect(errorRedirect);
            return;
        }

        try {

            UserLoginRequest userLoginRequest = new UserLoginRequest();
            userLoginRequest.setEmail(email);
            userLoginRequest.setName(name);
            UserLoginResponse userLoginResponse = userFeignClient.handleUserLogin(userLoginRequest);
            String token = jwtUtil.generateToken(userLoginResponse.getUserId(), email, userLoginResponse.getRole(), name);
            String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("token", token)
                    .build().toUriString();
            response.sendRedirect(targetUrl);

        } catch (FeignException e) {
            // Loguj za backend
            e.printStackTrace(); // Ili koristi logger
            System.out.println(e.getMessage());

            // Provjeri da li je uzrok blokiran korisnik
            String errorCode = e.status() == 403 ? "user_blocked" : "login_failed";

            String errorRedirect = UriComponentsBuilder.fromUriString(redirectUrl.replace("login-success", "login-error"))
                    .queryParam("error", errorCode)
                    .build().toUriString();

            response.sendRedirect(errorRedirect);
        }


    }
}