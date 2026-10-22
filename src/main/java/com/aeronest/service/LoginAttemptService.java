package com.aeronest.service;

import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private static final int MAX_ATTEMPTS = 5;
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt == null) return false;
        if (attempt.blockedUntil() != null && attempt.blockedUntil().isAfter(Instant.now())) return true;
        if (attempt.blockedUntil() != null) attempts.remove(key);
        return false;
    }

    public void failed(String key) {
        attempts.compute(key, (ignored, old) -> {
            int count = old == null ? 1 : old.count() + 1;
            return new Attempt(count, count >= MAX_ATTEMPTS ? Instant.now().plus(15, ChronoUnit.MINUTES) : null);
        });
    }

    public void succeeded(String key) { attempts.remove(key); }
    private record Attempt(int count, Instant blockedUntil) {}
}
