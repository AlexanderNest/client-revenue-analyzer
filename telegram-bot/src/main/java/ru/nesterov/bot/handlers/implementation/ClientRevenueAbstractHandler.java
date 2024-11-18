package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.CommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.util.ArrayList;
import java.util.List;

public abstract class ClientRevenueAbstractHandler implements CommandHandler {
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
    protected InlineKeyboardButton buildButton(String visibleText, String callbackValue) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(visibleText);
        ButtonCallback buttonCallback = new ButtonCallback();
        buttonCallback.setCommand(getCommand());
        buttonCallback.setValue(callbackValue);

        button.setCallbackData(buttonCallback.toShortString());
        return button;
    }

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

    protected BotApiMethod<?> sendKeyBoardWithYesNoButtons(long chatId, String text) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true"));
        rowInline.add(buildButton("Нет", "false"));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(chatId, text, keyboardMarkup);
    }

    protected String getButtonCallbackValue(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        ButtonCallback buttonCallback = ButtonCallback.fromShortString(callbackData);

        return buttonCallback.getValue();
    }

    public abstract String getCommand();
}