package com.radius.gateway.controller;

import java.util.HashMap;
import java.util.Map;

import com.radius.gateway.service.RadiusOAuth2UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Returns 200 with user info if the session is authenticated,
     * or 401 if anonymous / no valid session.
     *
     * This endpoint is permitAll() in SecurityConfig so it never
     * triggers an OAuth2 redirect — the frontend can safely call it
     * on a page load to decide whether to show login or the app.
     */
    @GetMapping("/status")
    public ResponseEntity<?> status(Authentication authentication) {
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("authenticated", false));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("authenticated", true);
        body.put("email", "");
        body.put("name", "");
        body.put("username", "");

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            String email = oauth2Token.getPrincipal().getAttribute("email");
            String name = oauth2Token.getPrincipal().getAttribute("name");
            String username = oauth2Token.getPrincipal()
                    .getAttribute(RadiusOAuth2UserService.RADIUS_USERNAME_ATTRIBUTE);
            if (email != null) body.put("email", email);
            if (name != null) body.put("name", name);
            if (username != null) body.put("username", username);
        }

        return ResponseEntity.ok(body);
    }
}
