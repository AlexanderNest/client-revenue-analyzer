package ru.nesterov.bot.handlers.abstractions;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.callback.ButtonCallback;

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

        CallbackQuery callbackQuery = update.getCallbackQuery();

        boolean isCallback = callbackQuery != null
                && (getCommand().equals(ButtonCallback.fromShortString(callbackQuery.getData()).getCommand()) || getCommand().equals(objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class).getCommand()));
        boolean isPlainText = message != null && message.getText() != null;

        return isCurrentHandlerCommand || isCallback || (isPlainText && !isFinished(TelegramUpdateUtils.getUserId(update)));
    }
}
