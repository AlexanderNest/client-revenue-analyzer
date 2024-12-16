package ru.nesterov.bot.handlers.abstractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

/**
 * CommandHandler, который отправляет сообщения. Также содержит полезные методы для быстрого создания сообщений.
 * В том числе и с клавиатурами, коллбеками и др.
 */
public abstract class SendingMessageCommandHandler implements CommandHandler {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected ClientRevenueAnalyzerIntegrationClient client;

    public BotApiMethod<?> getPlainSendMessage(long chatId, String text) {
        return buildSendMessage(chatId, text, null);
    }

    public BotApiMethod<?> getReplyKeyboard(long chatId, String text, ReplyKeyboard replyKeyboard) {
        return buildSendMessage(chatId, text, replyKeyboard);
    }

    public EditMessageText editMessage(long chatId, int messageId, String text, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(keyboardMarkup);

        return editMessageText;
    }

    private SendMessage buildSendMessage(long chatId, String text, ReplyKeyboard replyKeyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(replyKeyboard);

        return message;
    }

    public BotApiMethod<?> getCallbackQuery(Update update, String message) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText(message);

        return answerCallbackQuery;
    }

    /**
     *
     * @param visibleText текст, который будет отображаться на кнопке
     * @param callbackValue значение, связанное с кнопкой
     * @return созданная кнопка
     */
    protected InlineKeyboardButton buildButton(String visibleText, String callbackValue, String command) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(visibleText);
        ButtonCallback buttonCallback = new ButtonCallback();
        buttonCallback.setCommand(command);
        buttonCallback.setValue(callbackValue);

        button.setCallbackData(buttonCallback.toShortString());
        return button;
    }
}