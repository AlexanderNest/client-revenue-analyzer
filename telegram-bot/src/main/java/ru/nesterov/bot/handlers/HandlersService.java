package ru.nesterov.bot.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class HandlersService {
    private final Map<Long, CommandHandler> userHandlers = new ConcurrentHashMap<>();
    private final List<CommandHandler> commandHandlers;

    @Nullable
    public CommandHandler getHandler(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler userHandler = userHandlers.get(userId);
        if (userHandler != null && userHandler.isApplicable(update)) {
            return userHandler;
        } else {
            userHandlers.remove(userId);
        }

        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.isApplicable(update)) {
                userHandlers.put(userId, commandHandler);
                return commandHandler;
            }
        }

        log.warn("Не удалось найти Handler для этого Update [{}]", update);
        return null;
    }

    public void resetHandlers(Long userId) {
        userHandlers.remove(userId);
    }
}