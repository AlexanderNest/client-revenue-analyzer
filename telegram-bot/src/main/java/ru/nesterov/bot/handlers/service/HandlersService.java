package ru.nesterov.bot.handlers.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.implementation.CancelCommandHandler;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class HandlersService {
    private final List<CommandHandler> highestPriorityCommandHandlers;
    private final List<CommandHandler> normalPriorityCommandHandlers;
    private final List<CommandHandler> lowestPriorityCommandHandlers;
    private final BotHandlersRequestsKeeper botHandlersRequestsKeeper;
    private final CancelCommandHandler cancelCommandHandler;

    private final Map<Long, CommandHandler> startedUserHandlers = new ConcurrentHashMap<>();

    public HandlersService(List<CommandHandler> commandHandlers, BotHandlersRequestsKeeper botHandlersRequestsKeeper,
                           CancelCommandHandler cancelCommandHandler) {

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
        this.cancelCommandHandler = cancelCommandHandler;
    }

    @Nullable
    public CommandHandler getHandler(Update update) {
        CommandHandler commandHandler;

        if (cancelCommandHandler.isApplicable(update)) {
            return cancelCommandHandler;
        }

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
        CommandHandler commandHandler = startedUserHandlers.remove(userId);
        if (commandHandler != null) {
            botHandlersRequestsKeeper.removeRequest(commandHandler.getClass(), userId);
        }
    }

    private CommandHandler getStartedHandler(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler userHandler = startedUserHandlers.get(userId);
        if (userHandler != null && userHandler.isApplicable(update)) {
            return userHandler;
        }

        startedUserHandlers.remove(userId);
        return null;
    }

    private CommandHandler selectHandler(List<CommandHandler> commandHandlers, Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.isApplicable(update)) {
                startedUserHandlers.put(userId, commandHandler);
                return commandHandler;
            }
        }

        return null;
    }

}
