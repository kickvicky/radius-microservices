package com.radius.gateway.service;

import com.radius.gateway.entity.User;
import com.radius.gateway.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * OIDC-aware user service that provisions a Radius user on first login.
 *
 * WHY THIS EXISTS (and why RadiusOAuth2UserService alone isn't enough):
 * Google OAuth2 login uses OpenID Connect because the "openid" scope is
 * requested in application.yaml. For the OIDC flow Spring Security calls
 * OidcUserService, NOT the plain OAuth2UserService — so we MUST register
 * an OIDC-specific service via .oidcUserService(...) in SecurityConfig.
 *
 * Flow:
 *   1. super.loadUser() performs the normal ID-token + UserInfo fetch.
 *   2. Extract "sub" and "email" from the OidcUser.
 *   3. Look up/provision a row in the users table.
 *   4. Return a DefaultOidcUser whose UserInfo claims include
 *      "radius_username" so the downstream principal carries it.
 */
@Service
public class RadiusOidcUserService extends OidcUserService {

    /** Custom claim key exposed on the authenticated principal. */
    public static final String RADIUS_USERNAME_ATTRIBUTE = "radius_username";

    private static final String SUB_ATTRIBUTE = "sub";
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;

    public RadiusOidcUserService(UserRepository userRepository,
                                 UsernameGenerator usernameGenerator) {
        this.userRepository = userRepository;
        this.usernameGenerator = usernameGenerator;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String googleSub = oidcUser.getSubject();
        String email = oidcUser.getEmail();

        if (googleSub == null || googleSub.isBlank()) {
            throw new OAuth2AuthenticationException("Google OIDC response missing required 'sub' claim");
        }
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google OIDC response missing required 'email' claim");
        }

        User user = userRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> provisionNewUser(googleSub, email));

        // Build a fresh UserInfo with our custom claim merged in. DefaultOidcUser's
        // getAttributes() is a merged view of ID-token + UserInfo claims, so adding
        // our claim to UserInfo surfaces it via principal.getAttribute(...).
        Map<String, Object> enrichedClaims = new HashMap<>();
        if (oidcUser.getUserInfo() != null) {
            enrichedClaims.putAll(oidcUser.getUserInfo().getClaims());
        }
        enrichedClaims.put(RADIUS_USERNAME_ATTRIBUTE, user.getUsername());

        OidcUserInfo enrichedUserInfo = new OidcUserInfo(enrichedClaims);

        return new DefaultOidcUser(
                Set.of(new SimpleGrantedAuthority(DEFAULT_ROLE)),
                oidcUser.getIdToken(),
                enrichedUserInfo,
                SUB_ATTRIBUTE
        );
    }

    private User provisionNewUser(String googleSub, String email) {
        User newUser = User.builder()
                .googleSub(googleSub)
                .email(email)
                .username(usernameGenerator.generateUnique())
                .build();
        return userRepository.save(newUser);
    }
}
