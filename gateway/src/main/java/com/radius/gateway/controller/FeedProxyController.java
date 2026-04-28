package com.radius.gateway.controller;

import com.radius.gateway.service.RadiusOAuth2UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Proxies feed requests from the gateway (8080) to the feed-service (8081).
 *
 * This replaces Spring Cloud Gateway's YAML/DSL routing because the
 * gateway-server-webmvc 4.3.3 auto-configuration is not loading routes.
 * The SecurityFilterChain still protects these endpoints — only authenticated
 * users (with a valid RADIUS_SESSION) can reach them.
 *
 * Every outbound call carries an X-Radius-Username header sourced from the
 * authenticated principal (see {@link #radiusUsernameInterceptor()}).
 */
@RestController
public class FeedProxyController {

    /** Header name on outbound requests to the feed service. */
    static final String X_RADIUS_USERNAME = "X-Radius-Username";

    /** Header carrying the internal Radius user UUID on outbound requests. */
    static final String X_RADIUS_USER_ID = "X-Radius-User-Id";

    private final RestClient restClient;

    public FeedProxyController(RestClient.Builder builder) {
        this.restClient = builder
                .baseUrl("http://localhost:8081")
                .requestInterceptor(radiusIdentityInterceptor())
                .build();
    }

    /**
     * Reads the Radius identity attributes from the currently authenticated principal
     * (populated at login by {@code RadiusOidcUserService} / {@code RadiusOAuth2UserService})
     * and forwards them to the feed service as {@value #X_RADIUS_USERNAME} and
     * {@value #X_RADIUS_USER_ID} on every outbound request.
     *
     * The feed service trusts these headers because it lives behind the gateway —
     * the SecurityFilterChain rejects unauthenticated traffic before it can ever
     * reach this controller, so by the time the interceptor runs there is always
     * a valid OAuth2 principal in the SecurityContext.
     */
    private static ClientHttpRequestInterceptor radiusIdentityInterceptor() {
        return (request, body, execution) -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof OAuth2AuthenticationToken oauth2Token) {
                OAuth2User principal = oauth2Token.getPrincipal();
                String username = principal.getAttribute(
                        RadiusOAuth2UserService.RADIUS_USERNAME_ATTRIBUTE);
                if (username != null) {
                    request.getHeaders().add(X_RADIUS_USERNAME, username);
                }
                String userId = principal.getAttribute(
                        RadiusOAuth2UserService.RADIUS_USER_ID_ATTRIBUTE);
                if (userId != null) {
                    request.getHeaders().add(X_RADIUS_USER_ID, userId);
                }
            }
            return execution.execute(request, body);
        };
    }

    @GetMapping("/api/posts")
    public ResponseEntity<String> getPosts(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {

        var uriBuilder = UriComponentsBuilder.fromPath("/api/posts");
        if (lat != null) uriBuilder.queryParam("lat", lat);
        if (lng != null) uriBuilder.queryParam("lng", lng);

        var response = restClient.get()
                .uri(uriBuilder.toUriString())
                .retrieve()
                .toEntity(String.class);

        return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }

    @PostMapping("/api/post")
    public ResponseEntity<String> createPost(@RequestBody String body) {
        var response = restClient.post()
                .uri("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(String.class);

        return ResponseEntity.status(response.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response.getBody());
    }
}
