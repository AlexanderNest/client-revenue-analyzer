package ru.nesterov.bot.handlers.implementation;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.AbstractHandler;

public class AddClientHandler extends AbstractHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        return null;
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
