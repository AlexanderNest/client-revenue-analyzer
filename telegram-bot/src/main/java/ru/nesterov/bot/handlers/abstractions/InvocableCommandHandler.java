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
