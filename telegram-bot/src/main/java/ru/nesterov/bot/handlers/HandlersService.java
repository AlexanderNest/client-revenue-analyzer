package ru.nesterov.bot.handlers;

import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.implementation.CreateUserHandler;
import ru.nesterov.bot.handlers.implementation.GetMonthStatisticsHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class HandlersService {
    private final GetMonthStatisticsHandler getMonthStatisticsHandler;
    private final CreateUserHandler createNewUserHandler;

    private final Map<Long, CommandHandler> userHandlers = new HashMap<>(); //thread safety

    private final List<CommandHandler> commandHandlers = new ArrayList<>();

    @PostConstruct
    private void init() {
        commandHandlers.add(getMonthStatisticsHandler);
        commandHandlers.add(createNewUserHandler);
    }

    @Nullable
    public CommandHandler getHandler(Update update) {
        CommandHandler userHandler = userHandlers.get(update.getMessage().getFrom().getId());
        if (userHandler != null) {
            return userHandler;
        }

        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.isApplicable(update)) {
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
