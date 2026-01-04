package ru.nesterov.core.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
public class KeyboardCacheService {
    private static final String KEY_PREFIX = "keyboard:last-update:";

    private final StringRedisTemplate redisTemplate;

    public KeyboardCacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean shouldUpdateKeyboard(Long chatId) {
        String key = KEY_PREFIX + chatId;

        String savedDate = redisTemplate.opsForValue().get(key);
        String today = LocalDate.now().toString();

        if (!today.equals(savedDate)) {
            redisTemplate.opsForValue().set(key, today, 1, TimeUnit.DAYS);
            return true;
        }

        return false;
    }
}
