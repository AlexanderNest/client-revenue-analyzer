package ru.nesterov.bot.handlers.implementation.invocable;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;

public class GetClientsStatisticsHandler extends DisplayedCommandHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        return null;
    }

    @Override
    public String getCommand() {
        return null;
    }
}
