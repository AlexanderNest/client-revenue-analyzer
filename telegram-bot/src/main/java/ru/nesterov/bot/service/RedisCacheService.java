package ru.nesterov.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.nesterov.bot.config.BotProperties;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisCacheService {
    private static final String KEY_PREFIX = "keyboard:last-update:";

    private final StringRedisTemplate redisTemplate;
    private final BotProperties botProperties;

    public boolean shouldUpdateKeyboard(Long chatId) {
        String key = KEY_PREFIX + chatId;

        Boolean hasKey = redisTemplate.hasKey(key);

        if (hasKey) {
            return false;
        }

        redisTemplate.opsForValue().set(key, LocalDateTime.now().toString(), botProperties.getButtonsUpdateIntervalHours(), TimeUnit.SECONDS);
        return true;
    }
}
