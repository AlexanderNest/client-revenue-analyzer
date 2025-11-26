package ru.nesterov.bot.handlers.abstractions;

import lombok.SneakyThrows;
import org.springframework.core.Ordered;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.dto.GetUserRequest;
import ru.nesterov.bot.dto.GetUserResponse;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
     * Определяет, будет ли обработчик отображаться для текущего update
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

    @SneakyThrows
    public List<BotApiMethod<?>> handleCommandInputAndSendClientNamesKeyboard(Update update, String text) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<GetActiveClientResponse> clients = client.getActiveClients(TelegramUpdateUtils.getUserId(update));

        if (clients.isEmpty()) {
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Нет доступных клиентов");
        }

        clients.sort(Comparator.comparing(GetActiveClientResponse::getName, String.CASE_INSENSITIVE_ORDER));

        for (GetActiveClientResponse response : clients) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(response.getName());
            ButtonCallback callback = new ButtonCallback();
            callback.setCommand(getCommand());
            callback.setValue(response.getName());
            button.setCallbackData(buttonCallbackService.getTelegramButtonCallbackString(callback));

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            keyboard.add(rowInline);
        }
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), text, keyboardMarkup);
    }

    public List<BotApiMethod<?>> handleApproveKeyBoard(Update update, String message) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), message, keyboardMarkup);
    }
}
