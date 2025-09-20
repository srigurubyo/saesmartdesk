package com.sae.smartdesk.auth.service;

import java.time.Instant;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class MfaEnrollmentCache {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);
    private final Map<String, Session> cache = new ConcurrentHashMap<>();

    public String put(UUID userId, String secret, boolean enrollment) {
        String token = UUID.randomUUID().toString();
        cache.put(token, new Session(userId, secret, Instant.now().plus(DEFAULT_TTL), enrollment));
        return token;
    }

    public Optional<Session> get(String token) {
        Session session = cache.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            cache.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    public void remove(String token) {
        cache.remove(token);
    }

    public record Session(UUID userId, String secret, Instant expiresAt, boolean enrollment) {
    }
}
