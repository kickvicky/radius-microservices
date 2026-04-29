package com.radius.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisIndexedHttpSession;

/**
 * Redis-backed HTTP session configuration.
 *
 * <h2>Why this exists</h2>
 * Default Tomcat {@code HttpSession} lives in JVM memory — lost on restart and
 * not shared across pods. Spring Session intercepts {@code request.getSession()}
 * via {@code SessionRepositoryFilter} and stores attributes in Redis instead, so
 * the {@code SecurityContext} and {@code OAuth2AuthorizedClient} (Google access
 * and refresh tokens) survive restarts and are visible to every gateway pod
 * behind the load balancer.
 *
 * <h2>Why the {@code @EnableRedisIndexedHttpSession} annotation is required</h2>
 * In Spring Boot 3.x, {@code SessionAutoConfiguration} lived in
 * {@code spring-boot-autoconfigure} and was activated automatically when
 * {@code spring-session-data-redis} appeared on the classpath. Spring Boot 4.0
 * extracted autoconfig into per-feature modules (e.g. {@code spring-boot-data-redis}),
 * and there is currently <strong>no Spring Boot 4.0 module shipping Spring Session
 * autoconfig</strong>. As a result, {@code spring.session.*} properties are read
 * by no one, and the {@code SessionRepositoryFilter} is never registered unless
 * we wire it in explicitly. This annotation is that explicit wiring — it imports
 * {@code RedisIndexedHttpSessionConfiguration}, which registers the
 * {@code RedisIndexedSessionRepository}, the filter, and the keyspace-notifications
 * initializer (needed for {@code SessionDestroyedEvent}).
 *
 * <h2>Why <i>indexed</i> rather than the simpler {@code @EnableRedisHttpSession}</h2>
 * The indexed variant enables:
 * <ul>
 *   <li>{@code findByPrincipalName(...)} — required if we ever build "log out
 *       all my sessions" or admin-driven user revocation.</li>
 *   <li>{@code SessionDestroyedEvent} firing on TTL expiry — useful for audit
 *       and for clearing any local caches keyed on session id.</li>
 * </ul>
 * Cost is one extra Redis key per session for the index; negligible.
 *
 * <h2>Why JDK serialization</h2>
 * Spring Security objects ({@code SecurityContextImpl},
 * {@code OAuth2AuthenticationToken}, {@code DefaultOidcUser},
 * {@code DefaultOAuth2AuthorizedClient}) implement {@code Serializable} and
 * contain complex graphs (claim maps, {@code Instant}s, authority sets).
 * JSON serialization of these requires registering Jackson mixins for every
 * Spring Security class via {@code SecurityJackson2Modules} — fragile and
 * version-sensitive across Spring upgrades. JDK is also Spring Session's
 * default; we declare it explicitly so future maintainers don't accidentally
 * swap it without understanding the impact on OAuth2 deserialization.
 *
 * <h2>Configuration source of truth</h2>
 * Because Boot 4.0 doesn't read {@code spring.session.*}, the timeout and
 * namespace live on the annotation below. Redis connection details
 * ({@code spring.data.redis.*}) are still read by {@code DataRedisAutoConfiguration}
 * and remain in {@code application.yaml}.
 */
@Configuration
@EnableRedisIndexedHttpSession(
        // 8 hours — must be kept in sync with server.servlet.session.timeout
        // and the cookie max-age in application.yaml.
        maxInactiveIntervalInSeconds = 28_800,
        redisNamespace = "radius:gateway:session"
)
public class RedisConfig {

    /**
     * Serializer used by Spring Session to write/read session attributes to/from
     * Redis. The bean name is significant — Spring Session resolves exactly this
     * name; renaming it silently falls back to the framework default.
     */
    @Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new JdkSerializationRedisSerializer();
    }
}
