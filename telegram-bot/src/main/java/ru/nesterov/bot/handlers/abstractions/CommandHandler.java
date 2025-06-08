package ru.nesterov.bot.handlers.abstractions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Любой обработчик
 */
public interface CommandHandler {
    /**
     * Метод для обработки полученного обновления в чате
     */
    BotApiMethod<?> handle(Update update);

    /**
     * Определяет применимость обработчика для данного обновления
     */
    boolean isApplicable(Update update);

    /**
     * Приоритет, с которым обработчики будут проверяться на соответствие
     */
    default Priority getPriority() {
        return Priority.NORMAL;
    }
}
