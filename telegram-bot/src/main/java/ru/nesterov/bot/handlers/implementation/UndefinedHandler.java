package ru.nesterov.bot.handlers.implementation;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.SendingMessageCommandHandler;

@Component
public class UndefinedHandler extends SendingMessageCommandHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        return getPlainSendMessage(TelegramUpdateUtils.getUserId(update), update.getMessage().getText());
    }

    @Override
    public boolean isApplicable(Update update) {
        return false;
    }

    @Override
    public boolean isFinished(Long userId) {
        return false;
    }
}
