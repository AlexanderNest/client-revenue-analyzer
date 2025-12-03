package ru.nesterov.bot.handlers.abstractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.service.ButtonCallbackService;
import ru.nesterov.bot.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * CommandHandler, который отправляет сообщения. Также содержит полезные методы для быстрого создания сообщений.
 * В том числе и с клавиатурами, коллбеками и др.
 */
public abstract class SendingMessageCommandHandler implements CommandHandler {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected ClientRevenueAnalyzerIntegrationClient client;
    @Autowired
    @Lazy
    protected ButtonCallbackService buttonCallbackService;

    /**
     * Вернет метод для отправки простого сообщения в чат
     * @param text - сообщение для отправки
     */
    public List<BotApiMethod<?>> getPlainSendMessage(long chatId, String text) {
        return List.of(buildSendMessage(chatId, text, null));
    }

    /**
     * Метод для отправки текстового сообщения с клавиатурой в чат
     */
    public List<BotApiMethod<?>> getReplyKeyboard(long chatId, String text, ReplyKeyboard replyKeyboard) {
        return List.of(buildSendMessage(chatId, text, replyKeyboard));
    }

    /**
     * Метод для изменения уже отправленного сообщения в чат
     * @param keyboardMarkup - если в сообщении нужно добавить клавиатуру
     */
    public List<BotApiMethod<?>> editMessage(long chatId, int messageId, String text, @Nullable InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(keyboardMarkup);

        return List.of(editMessageText);
    }

    /**
     * Метод для отправки всплывающего сообщения. Оно появится на некоторое время поверх чата, потом исчезнет
     */
    public BotApiMethod<?> getCallbackQuery(Update update, String message) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText(message);

        return answerCallbackQuery;
    }

    private SendMessage buildSendMessage(long chatId, String text, ReplyKeyboard replyKeyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(replyKeyboard);

        return message;
    }

    /**
     * Собирает inline кнопку с указанным значением для коллбека
     *
     * @param visibleText текст, который будет отображаться на кнопке
     * @param callbackValue значение коллбека для этой кнопки
     * @return созданная кнопка
     */
    protected InlineKeyboardButton buildButton(String visibleText, String callbackValue, String command) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(visibleText);
        ButtonCallback buttonCallback = new ButtonCallback();
        buttonCallback.setCommand(command);
        buttonCallback.setValue(callbackValue);

        button.setCallbackData(buttonCallbackService.getTelegramButtonCallbackString(buttonCallback));
        return button;
    }
    @SneakyThrows
    public List<BotApiMethod<?>> getClientNamesKeyboardAndSend(Update update, String text, String command) {
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
            callback.setCommand(command);
            callback.setValue(response.getName());
            button.setCallbackData(buttonCallbackService.getTelegramButtonCallbackString(callback));

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            keyboard.add(rowInline);
        }
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), text, keyboardMarkup);
    }

    public List<BotApiMethod<?>> getApproveKeyBoard(Update update, String message, String command) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(buildButton("Да", "true", command));
        rowInline.add(buildButton("Нет", "false", command));
        keyboard.add(rowInline);
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), message, keyboardMarkup);
    }
}
