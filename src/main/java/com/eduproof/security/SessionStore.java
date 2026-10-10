package com.eduproof.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionStore {
    private final Map<String, Long> tokens = new ConcurrentHashMap<>();

    public String create(Long userId) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, userId);
        return token;
    }

    public Long getUserId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return tokens.get(token);
    }

    public void invalidate(String token) {
        if (token != null) {
            tokens.remove(token);
        }
    }
}
