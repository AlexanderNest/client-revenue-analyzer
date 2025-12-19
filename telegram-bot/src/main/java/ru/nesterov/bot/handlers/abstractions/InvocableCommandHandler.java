package ru.nesterov.bot.handlers.abstractions;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.utils.TelegramUpdateUtils;
import ru.nesterov.core.entity.Role;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Обработчик, который вызывается по отправленной команде
 */
public abstract class InvocableCommandHandler extends SendingMessageCommandHandler {
    /**
     * Команда, которая вызовет обработчик
     */
    public abstract String getCommand();

    protected List<Role> getApplicableRoles() {
        return List.of(Role.USER);
    }

    public List<BotApiMethod<?>> getClientNamesKeyboard(Update update, String text) {
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

    public List<BotApiMethod<?>> getApproveKeyBoardMessage(Update update, String message) {
        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), message, getApproveKeyboard());
    }

    public InlineKeyboardMarkup getApproveKeyboard() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", getCommand()));
        rowInline.add(buildButton("Нет", "false", getCommand()));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);

        return keyboardMarkup;
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();

        boolean isCurrentHandlerCommand = message != null && getCommand().equals(message.getText());
        if (isCurrentHandlerCommand) {
            return true;
        }

        CallbackQuery callbackQuery = update.getCallbackQuery();

        if (callbackQuery == null) {
            return false;
        }

        boolean isShortButtonCallback = getCommand().equals(buttonCallbackService.buildButtonCallback(callbackQuery.getData()).getCommand());
        boolean isJsonButtonCallback = false;
        try {
            isJsonButtonCallback = getCommand().equals(objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class).getCommand());
        } catch (Exception ignored) {

        }

        return isShortButtonCallback || isJsonButtonCallback;
    }
}
