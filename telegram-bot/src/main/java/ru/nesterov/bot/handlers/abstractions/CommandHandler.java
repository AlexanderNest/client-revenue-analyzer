package ru.nesterov.bot.handlers.abstractions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Любой обработчик
 */
public interface CommandHandler {
    BotApiMethod<?> handle(Update update);
    boolean isApplicable(Update update);
    boolean isFinished(Long userId);
}
