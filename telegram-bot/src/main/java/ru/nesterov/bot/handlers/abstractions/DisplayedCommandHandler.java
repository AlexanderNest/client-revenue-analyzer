package ru.nesterov.bot.handlers.abstractions;

import org.springframework.core.Ordered;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.dto.GetUserRequest;
import ru.nesterov.bot.dto.GetUserResponse;
import ru.nesterov.bot.utils.TelegramUpdateUtils;
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

    public boolean isDisplayed(Update update) {
        GetUserRequest getUserRequest = new GetUserRequest();
        getUserRequest.setUsername(String.valueOf(TelegramUpdateUtils.getUserId(update)));
        GetUserResponse response = client.getUserByUsername(getUserRequest);
        if (response != null) {
            return isDisplayedForRegistered() && isDisplayedForRole(response);
        } else {
            return isDisplayedForUnregistered();
        }
    }

    private boolean isDisplayedForRole(GetUserResponse response) {
        return getApplicableRoles().contains(response.getRole());
    }

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
