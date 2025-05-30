package ru.nesterov.bot.handlers.implementation;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.service.HandlersService;

@Component
public class CancelCommandHandler extends InvocableCommandHandler {
    @Resource
    @Lazy
    private HandlersService handlersService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        handlersService.resetAllHandlers(userId);
        return getPlainSendMessage(userId, "Контекст сброшен");
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
