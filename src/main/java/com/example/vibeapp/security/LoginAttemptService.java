package com.example.vibeapp.security;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    private static final String FAILURE_PREFIX = "login:failure:";
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCK_TIME_MINUTES = 15;

    private final RedisTemplate<String, Object> redisTemplate;

    public LoginAttemptService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void loginFailed(String email) {
        String key = FAILURE_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);
        int attempts = (value == null) ? 0 : Integer.parseInt(value.toString());
        attempts++;

        redisTemplate.opsForValue().set(key, String.valueOf(attempts), LOCK_TIME_MINUTES, TimeUnit.MINUTES);
    }

    public void loginSucceeded(String email) {
        redisTemplate.delete(FAILURE_PREFIX + email);
    }

    public boolean isLocked(String email) {
        String key = FAILURE_PREFIX + email;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null && Integer.parseInt(value.toString()) >= MAX_ATTEMPTS;
    }
}
