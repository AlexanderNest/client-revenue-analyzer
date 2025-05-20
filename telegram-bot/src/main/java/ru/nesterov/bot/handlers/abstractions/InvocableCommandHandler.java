package ru.nesterov.bot.handlers.abstractions;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.service.ActionService;
import ru.nesterov.statemachine.dto.Action;

/**
 * Обработчик, который вызывается по отправленной команде
 */
public abstract class InvocableCommandHandler extends SendingMessageCommandHandler {
    public abstract String getCommand();

    @Override
    @SneakyThrows
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        boolean isCurrentHandlerCommand = message != null && getCommand().equals(message.getText());

        if (isCurrentHandlerCommand) {
            return true;
        }

//        boolean isPlainText = message != null && message.getText() != null;
//        if (isPlainText && !isFinished(TelegramUpdateUtils.getUserId(update))) {
//            return true;
//        }

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
