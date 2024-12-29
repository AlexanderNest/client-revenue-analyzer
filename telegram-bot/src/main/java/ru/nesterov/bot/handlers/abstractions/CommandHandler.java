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
     * Определяет нужно ли сбрасывать обработчик для указанного пользователя.
     * Это полезно для тех случаев, когда один и тот же обработчик должен получить несколько сообщений подряд.
     * @param userId
     * @return
     *      true - если надо сбросить обработчики для пользователя.
     *      false - если надо, чтобы при следующем обновлении в чате вызвался тот же обработчик
     */
    boolean isFinished(Long userId);
}
