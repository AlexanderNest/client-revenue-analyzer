package ru.nesterov.bot.handlers.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@ConditionalOnProperty("bot.enabled")
public class HandlersService {
    private final List<CommandHandler> highestPriorityCommandHandlers;
    private final List<CommandHandler> normalPriorityCommandHandlers;
    private final List<CommandHandler> lowestPriorityCommandHandlers;
    private final BotHandlersRequestsKeeper botHandlersRequestsKeeper;

    private final List<StatefulCommandHandler<?, ?>> statefulCommandHandlers;

    public HandlersService(List<CommandHandler> commandHandlers,
                           List<StatefulCommandHandler<?, ?>> statefulCommandHandlers,
                           BotHandlersRequestsKeeper botHandlersRequestsKeeper) {
        this.statefulCommandHandlers = statefulCommandHandlers;

        highestPriorityCommandHandlers = commandHandlers.stream()
                .filter(ch -> ch.getPriority() == Priority.HIGHEST)
                .toList();

        normalPriorityCommandHandlers = commandHandlers.stream()
                .filter(ch -> ch.getPriority() == Priority.NORMAL)
                .toList();

        lowestPriorityCommandHandlers = commandHandlers.stream()
                .filter(ch -> ch.getPriority() == Priority.LOWEST)
                .toList();

        this.botHandlersRequestsKeeper = botHandlersRequestsKeeper;
    }

    @Nullable
    public CommandHandler getHandler(Update update) {
        CommandHandler commandHandler;

        commandHandler = getStartedHandler(update);
        if (commandHandler != null) {
            return commandHandler;
        }

        commandHandler = selectHandler(highestPriorityCommandHandlers, update);
        if (commandHandler != null) {
            return commandHandler;
        }

        commandHandler = selectHandler(normalPriorityCommandHandlers, update);
        if (commandHandler != null) {
            return commandHandler;
        }

        commandHandler = selectHandler(lowestPriorityCommandHandlers, update);
        if (commandHandler != null) {
            return commandHandler;
        }

        log.warn("Не удалось найти Handler для этого Update [{}]", update);
        return null;
    }

    public void resetHandlers(Long userId) {
        statefulCommandHandlers.stream()
                .filter(handler -> handler.isFinished(userId))
                .forEach(handler -> handler.resetState(userId));
    }

    private CommandHandler getStartedHandler(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler commandHandler = statefulCommandHandlers.stream()
                .filter(handler -> !handler.isFinished(userId))
                .findFirst()
                .orElse(null);

        if (commandHandler != null) {
            if (commandHandler.isApplicable(update)) {
                return commandHandler;
            }
            else {
                throw new RuntimeException("Неподходящий commandHandler " + update + commandHandler);
            }
        }
        return null;
    }

    private CommandHandler selectHandler(List<CommandHandler> commandHandlers, Update update) {
        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.isApplicable(update)) {
                return commandHandler;
            }
        }

        return null;
    }
}
