package com.radius.gateway.controller;

import java.util.Map;

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

        String email = null;
        String name = null;

        if (authentication instanceof OAuth2AuthenticationToken oauth2Token) {
            email = oauth2Token.getPrincipal().getAttribute("email");
            name = oauth2Token.getPrincipal().getAttribute("name");
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "email", email != null ? email : "",
                "name", name != null ? name : ""
        ));
    }
}
