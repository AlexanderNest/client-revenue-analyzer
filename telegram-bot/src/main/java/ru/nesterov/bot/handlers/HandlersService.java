package ru.nesterov.bot.handlers;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.implementation.CreateUserHandler;
import ru.nesterov.bot.handlers.implementation.GetMonthStatisticsHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class HandlersService {
    private final GetMonthStatisticsHandler getMonthStatisticsHandler;
    private final CreateUserHandler createNewUserHandler;

    private final Map<Long, CommandHandler> userHandlers = new ConcurrentHashMap<>();

    private final List<CommandHandler> commandHandlers;

    @Nullable
    public CommandHandler getHandler(Update update) {
        long userId = getUserId(update);
        CommandHandler userHandler = userHandlers.get(userId);
        if (userHandler != null) {
            return userHandler;
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

    public long getUserId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        } else {
            return update.getCallbackQuery().getFrom().getId();
        }
    }

    public void resetHandlers(Long userId) {
        userHandlers.remove(userId);
    }
}
