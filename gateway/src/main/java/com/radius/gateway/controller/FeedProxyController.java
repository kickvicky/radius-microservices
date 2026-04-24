package com.radius.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 */
@RestController
public class FeedProxyController {

    private final RestClient restClient;

    public FeedProxyController(RestClient.Builder builder) {
        this.restClient = builder.baseUrl("http://localhost:8081").build();
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
