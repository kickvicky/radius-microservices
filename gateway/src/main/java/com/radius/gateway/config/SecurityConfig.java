package com.radius.gateway.config;

import java.util.List;

import com.radius.gateway.service.RadiusOAuth2UserService;
import com.radius.gateway.service.RadiusOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Gateway security configuration.
 *
 * Flow:
 *  1. User hits any protected endpoint → redirected to Google OAuth2 login.
 *  2. After successful login, RadiusOAuth2UserService provisions the user
 *     (first-login insert) and enriches the principal with a
 *     "radius_username" claim used by downstream services.
 *  3. A servlet session is created and persisted via the RADIUS_SESSION
 *     cookie (configured in application.yaml).
 *  4. On subsequent requests from the Next.js frontend (localhost:3000),
 *     the browser sends the RADIUS_SESSION cookie automatically.
 *  5. FeedProxyController proxies authenticated requests to downstream services.
 */
@Configuration
public class SecurityConfig {

    private final RadiusOAuth2UserService radiusOAuth2UserService;
    private final RadiusOidcUserService radiusOidcUserService;

    public SecurityConfig(RadiusOAuth2UserService radiusOAuth2UserService,
                          RadiusOidcUserService radiusOidcUserService) {
        this.radiusOAuth2UserService = radiusOAuth2UserService;
        this.radiusOidcUserService = radiusOidcUserService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF disabled for local development; enable with a CookieCsrfTokenRepository in production
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/public/**", "/api/auth/status").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .defaultSuccessUrl("http://localhost:3000", true)
                        // Plug in the custom user services so provisioning runs on every
                        // successful login and the principal carries radius_username.
                        // .oidcUserService(...)  → covers Google login (uses "openid" scope → OIDC flow)
                        // .userService(...)      → covers any future non-OIDC OAuth2 providers (e.g. GitHub)
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(radiusOidcUserService)
                                .userService(radiusOAuth2UserService)
                        )
                )
                // IF_REQUIRED: a session is created only when authentication occurs (OAuth2 login).
                // The RADIUS_SESSION cookie then maintains state for all subsequent requests.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

    /**
     * CORS policy for the Next.js frontend.
     * allowCredentials(true) is required so the browser includes the
     * RADIUS_SESSION cookie on cross-origin requests from localhost:3000.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
