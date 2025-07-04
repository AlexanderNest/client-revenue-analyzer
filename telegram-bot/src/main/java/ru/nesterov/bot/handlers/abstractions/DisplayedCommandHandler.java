package ru.nesterov.bot.handlers.abstractions;

import org.springframework.core.Ordered;

/**
 * Обработчик, который будет отображаться в списке команд для отправки на стороне пользователя
 */
public abstract class DisplayedCommandHandler extends InvocableCommandHandler implements Ordered {
    /**
     * Определяет порядок отображения обработчика на панели с кнопками
     */
    @Override
    public int getOrder() {
        return 5;
    }

    /**
     * Определяет, будет ли обработчик отображаться для зарегистрированных пользователей
     */
    public boolean isDisplayedForRegistered() {
        return true;
    }

    /**
     * Определяет, будет ли обработчик отображаться для незарегистрированных пользователей
     */
    public boolean isDisplayedForUnregistered() {
        return false;
    }
}
