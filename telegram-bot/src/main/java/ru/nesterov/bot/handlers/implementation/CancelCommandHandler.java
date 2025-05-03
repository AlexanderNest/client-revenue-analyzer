package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.bot.handlers.service.HandlersService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
@Component
public class CancelCommandHandler extends InvocableCommandHandler {
    private final BotHandlersRequestsKeeper botHandlersRequestsKeeper;
//    private final HandlersService handlersService;
    private final Map<Long, CommandHandler> startedUserHandlers = new ConcurrentHashMap<>();

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler commandHandler = startedUserHandlers.remove(userId);
        if (commandHandler != null) {
            botHandlersRequestsKeeper.removeRequest(commandHandler.getClass(), userId);
        }
//        handlersService.resetHandlers(userId);  
//        botHandlersRequestsKeeper.removeRequest(CommandHandler.class, userId);
        return getPlainSendMessage(userId, "Хендлеры отменены");
    }

    @Override
    public boolean isFinished(Long userId) {
        return true;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    @Override
    public String getCommand() {
        return "/cancel";
    }
}
