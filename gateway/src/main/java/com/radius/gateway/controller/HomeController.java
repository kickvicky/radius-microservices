package com.radius.gateway.controller;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HomeController {

    @GetMapping("/public")
    public String publicApi() {
        return "This is a public API endpoint.";
    }

    @GetMapping("/private")
    public String privateApi(OAuth2AuthenticationToken token) {
        return "This is a private API endpoint. You are authenticated - " + token.getPrincipal().getAttribute("email");
    }

}
