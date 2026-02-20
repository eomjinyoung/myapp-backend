package com.example.vibeapp.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    private static final String ROTATED_PREFIX = "rotated:token:";

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void addToBlacklist(String token, long expirationMs) {
        if (expirationMs > 0) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "logout", expirationMs, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    public void addRotatedToken(String token, String email, long expirationMs) {
        if (expirationMs > 0) {
            redisTemplate.opsForValue().set(ROTATED_PREFIX + token, email, expirationMs, TimeUnit.MILLISECONDS);
        }
    }

    public String getRotatedTokenUserEmail(String token) {
        Object email = redisTemplate.opsForValue().get(ROTATED_PREFIX + token);
        return email != null ? email.toString() : null;
    }
}
