package com.radius.gateway.repository;

import com.radius.gateway.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for {@link User}.
 *
 * findByGoogleSub is the primary lookup used during OAuth2 login — we key
 * on Google's stable "sub" claim rather than email, which a user can change.
 * existsByUsername supports the username generator's collision retry loop.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByGoogleSub(String googleSub);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
}
