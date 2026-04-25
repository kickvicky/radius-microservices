package com.radius.gateway.service;

import com.radius.gateway.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

/**
 * Generates Radius-style usernames in the pattern {Adjective}-{Animal}-{Number}
 * (e.g. "Spicy-Falcon-404").
 *
 * With 20 adjectives × 20 animals × 900 numbers (100–999) the keyspace is
 * 360,000 combinations per run, which is plenty for small-to-mid scale.
 * A collision-retry loop (bounded by MAX_ATTEMPTS) falls back to a longer
 * numeric suffix in the rare case a unique handle cannot be found quickly.
 */
@Service
public class UsernameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "Spicy", "Crispy", "Cheesy", "Tangy", "Smoky",
            "Fluffy", "Mighty", "Silent", "Cosmic", "Electric",
            "Golden", "Swift", "Brave", "Stealthy", "Wild",
            "Noble", "Frosty", "Shadow", "Crimson", "Stormy"
    );

    private static final List<String> ANIMALS = List.of(
            "Falcon", "Tiger", "Panda", "Phoenix", "Eagle",
            "Wolf", "Otter", "Raven", "Lynx", "Hawk",
            "Fox", "Owl", "Leopard", "Jaguar", "Heron",
            "Bear", "Sparrow", "Cheetah", "Lion", "Stork"
    );

    private static final int MAX_ATTEMPTS = 10;
    private static final int BASE_NUMBER_BOUND = 900;    // 100–999
    private static final int BASE_NUMBER_OFFSET = 100;
    private static final int FALLBACK_NUMBER_BOUND = 9_000_000;  // wider bucket on fallback

    private final SecureRandom random = new SecureRandom();
    private final UserRepository userRepository;

    public UsernameGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a username that is guaranteed to be unique in the database
     * at the moment of generation. Caller is still responsible for handling
     * the (extremely unlikely) race on concurrent inserts via the DB's
     * unique index on users.username.
     */
    public String generateUnique() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = buildUsername(BASE_NUMBER_BOUND, BASE_NUMBER_OFFSET);
            if (!userRepository.existsByUsername(candidate)) {
                return candidate;
            }
        }
        // Fallback: widen the number space to make a collision statistically negligible.
        return buildUsername(FALLBACK_NUMBER_BOUND, BASE_NUMBER_OFFSET);
    }

    private String buildUsername(int numberBound, int offset) {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        int number = random.nextInt(numberBound) + offset;
        return adjective + "-" + animal + "-" + number;
    }
}
