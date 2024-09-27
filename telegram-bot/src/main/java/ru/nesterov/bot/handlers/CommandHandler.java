package ru.nesterov.bot.handlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface CommandHandler {
    BotApiMethod<?> handle(Update update);
    boolean isApplicable(Update update);
}