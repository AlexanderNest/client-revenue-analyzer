package ru.nesterov.bot.handlers.service;

import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.implementation.UndefinedHandler;
import ru.nesterov.bot.handlers.implementation.invocable.CancelCommandHandler;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.List;
import java.util.function.Predicate;

@Component
@Slf4j
@ConditionalOnProperty("bot.enabled")
public class HandlersService {
    private final List<CommandHandler> highestPriorityCommandHandlers;
    private final List<CommandHandler> normalPriorityCommandHandlers;
    private final List<CommandHandler> lowestPriorityCommandHandlers;

    private final UndefinedHandler undefinedHandler;
    private final CancelCommandHandler cancelCommandHandler;

    private final List<StatefulCommandHandler<?, ?>> statefulCommandHandlers;

    public HandlersService(List<CommandHandler> commandHandlers,
                           List<StatefulCommandHandler<?, ?>> statefulCommandHandlers,
                           UndefinedHandler undefinedHandler,
                           CancelCommandHandler cancelCommandHandler) {
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

        this.undefinedHandler = undefinedHandler;
        this.cancelCommandHandler = cancelCommandHandler;
    }

    @Nullable
    public CommandHandler getHandler(Update update) {
        CommandHandler commandHandler;

        if (cancelCommandHandler.isApplicable(update)) {
            return cancelCommandHandler;
        }

        //Проверяем является ли update командой
        if (isCommandUpdate(update)){
            // Для любой команды сначала сбрасываем все обработчики
            long userId = TelegramUpdateUtils.getUserId(update);
            resetAllHandlers(userId);
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
        return undefinedHandler;
    }

    public void resetBrokeHandler(CommandHandler commandHandler, long userId) {
        if (commandHandler instanceof StatefulCommandHandler<?,?>) {
            ((StatefulCommandHandler<?, ?>) commandHandler).resetState(userId);
        }
    }

    public void resetFinishedHandlers(Long userId) {
        resetHandlers(userId, handler -> handler.isFinishedOrNotStarted(userId));
    }

    public void resetAllHandlers(Long userId) {
        resetHandlers(userId, handler -> true);
    }

    private void resetHandlers(Long userId, Predicate<StatefulCommandHandler<?, ?>> predicate) {
        statefulCommandHandlers.stream()
                .filter(predicate)
                .forEach(handler -> handler.resetState(userId));
    }

    private CommandHandler getStartedHandler(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler commandHandler = statefulCommandHandlers.stream()
                .filter(handler -> !handler.isFinishedOrNotStarted(userId))
                .findFirst()
                .orElse(null);

        if (commandHandler != null) {
            if (commandHandler.isApplicable(update)) {
                return commandHandler;
            } else {
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

    // Метод для проверки, является ли update командой
    private boolean isCommandUpdate(Update update) {
        // Проверяем все InvocableCommandHandler'ы
        for (CommandHandler handler : highestPriorityCommandHandlers) {
            if (handler instanceof InvocableCommandHandler && handler.isApplicable(update)) {
                return true;
            }
        }
        for (CommandHandler handler : normalPriorityCommandHandlers) {
            if (handler instanceof InvocableCommandHandler && handler.isApplicable(update)) {
                return true;
            }
        }
        for (CommandHandler handler : lowestPriorityCommandHandlers) {
            if (handler instanceof InvocableCommandHandler && handler.isApplicable(update)) {
                return true;
            }
        }
        return false;
    }
}
