package ru.nesterov.bot.handlers.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.implementation.UnregisteredUserHandler;

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

    private final UnregisteredUserHandler unregisteredUserHandler;

    @Nullable
    public CommandHandler getHandler(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler userHandler = userHandlers.get(userId);
        if (userHandler != null && userHandler.isApplicable(update)) {
            return userHandler;
        } else {
            userHandlers.remove(userId);
        }

        if (unregisteredUserHandler.isApplicable(update)) {
            log.debug("UnregisteredUserHandler был выбран вне очереди");
            return unregisteredUserHandler;
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
