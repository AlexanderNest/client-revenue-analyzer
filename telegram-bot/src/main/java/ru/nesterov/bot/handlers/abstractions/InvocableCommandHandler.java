package ru.nesterov.bot.handlers.abstractions;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.callback.ButtonCallback;

/**
 * Обработчик, который вызывается по отправленной команде
 */
public abstract class InvocableCommandHandler extends SendingMessageCommandHandler {
    /**
     * Команда, которая вызовет обработчик
     */
    public abstract String getCommand();

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();

        if (message != null && getCommand().equals(message.getText())) {
            return true;
        }

        CallbackQuery callbackQuery = update.getCallbackQuery();

        if (callbackQuery == null) {
            return false;
        }

        String data = callbackQuery.getData();

        boolean isShortButtonCallback = getCommand().equals(buttonCallbackService.buildButtonCallback(data).getCommand());
        if (isShortButtonCallback) {
            return true;
        }

        try {
            ButtonCallback buttonCallback = objectMapper.readValue(data, ButtonCallback.class);
            return getCommand().equals(buttonCallback.getCommand());
        } catch (Exception ignored) {
            return false;
        }
    }
}
