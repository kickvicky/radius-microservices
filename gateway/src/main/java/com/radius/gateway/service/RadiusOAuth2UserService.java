package com.radius.gateway.service;

import com.radius.gateway.entity.User;
import com.radius.gateway.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Custom OAuth2 user service that provisions a Radius user on first login
 * and enriches the authenticated principal with the Radius username.
 *
 * Flow (invoked by Spring Security after Google returns the user-info payload):
 *   1. Delegate to {@link DefaultOAuth2UserService} to fetch standard claims.
 *   2. Read "sub" and "email" from the returned attributes.
 *   3. Look up the user in PostgreSQL by googleSub.
 *        - If present → reuse the stored username.
 *        - If absent  → generate a unique username and insert a new row.
 *   4. Return a {@link DefaultOAuth2User} whose attribute map includes a
 *      "radius_username" claim. Downstream components (e.g. FeedProxyController)
 *      can read this via authentication.getPrincipal().getAttribute("radius_username")
 *      and forward it to the Feed Service.
 *
 * The default name-attribute key is preserved as "sub" so that
 * OAuth2User#getName() remains stable across logins.
 */
@Service
public class RadiusOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    /** Custom claim key exposed on the authenticated principal. */
    public static final String RADIUS_USERNAME_ATTRIBUTE = "radius_username";

    private static final String SUB_ATTRIBUTE = "sub";
    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final UserRepository userRepository;
    private final UsernameGenerator usernameGenerator;

    public RadiusOAuth2UserService(UserRepository userRepository,
                                   UsernameGenerator usernameGenerator) {
        this.userRepository = userRepository;
        this.usernameGenerator = usernameGenerator;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String googleSub = oauth2User.getAttribute(SUB_ATTRIBUTE);
        String email = oauth2User.getAttribute(EMAIL_ATTRIBUTE);

        if (googleSub == null || googleSub.isBlank()) {
            throw new OAuth2AuthenticationException("Google OAuth2 response missing required 'sub' claim");
        }
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google OAuth2 response missing required 'email' claim");
        }

        User user = userRepository.findByGoogleSub(googleSub)
                .orElseGet(() -> provisionNewUser(googleSub, email));

        // Clone attributes so we don't mutate the immutable map returned by DefaultOAuth2UserService.
        Map<String, Object> enrichedAttributes = new HashMap<>(oauth2User.getAttributes());
        enrichedAttributes.put(RADIUS_USERNAME_ATTRIBUTE, user.getUsername());

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority(DEFAULT_ROLE)),
                enrichedAttributes,
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
