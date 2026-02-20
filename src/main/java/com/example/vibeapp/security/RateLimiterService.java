package com.example.vibeapp.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimiterService {
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    private static final int LIMIT = 10;
    private static final int WINDOW_SECONDS = 10;

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimiterService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String ip, String endpoint) {
        String key = RATE_LIMIT_PREFIX + endpoint + ":" + ip;
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null || count == 1L) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }

        return count != null && count <= LIMIT;
    }
}
