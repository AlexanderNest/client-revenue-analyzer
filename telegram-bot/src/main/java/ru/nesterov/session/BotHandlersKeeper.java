package ru.nesterov.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.nesterov.bot.handlers.implementation.ClientRevenueAbstractHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class BotHandlersKeeper {
    private final Map<Class<? extends  ClientRevenueAbstractHandler>, Map<Long, Object>> handlersKeeper;

    @SuppressWarnings("unchecked")
    public <T> Map<Long, T> getHandlerKeeper(Class<? extends  ClientRevenueAbstractHandler> handler) {
        return (Map<Long, T>) handlersKeeper.computeIfAbsent(handler, key -> new ConcurrentHashMap<>());
    }
}