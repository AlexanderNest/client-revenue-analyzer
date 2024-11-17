package ru.nesterov.bot.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BotHandlersRequestsKeeper {
    private final Map<Class<?>, Map<Long, Object>> map = new ConcurrentHashMap<>();

    @Nullable
    public <T> T getRequest(long userId, Class<?> handlerType, Class<T> requestType) {
        Map<Long, Object> requests = map.get(handlerType);
        if (requests == null) {
            return null;
        }

        return (T) requests.get(userId);
    }

    public <T> T putRequest(Class<?> handlerType, long userId, T request) {
        Map<Long, Object> requests = map.computeIfAbsent(handlerType, key -> new ConcurrentHashMap<>());
        requests.put(userId, request);
        return request;
    }
}