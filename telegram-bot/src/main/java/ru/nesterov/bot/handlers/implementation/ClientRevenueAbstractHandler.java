package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.CommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

public abstract class ClientRevenueAbstractHandler implements CommandHandler {
    protected final ObjectMapper objectMapper;
    protected final ClientRevenueAnalyzerIntegrationClient client;

    public ClientRevenueAbstractHandler(ObjectMapper objectMapper, ClientRevenueAnalyzerIntegrationClient client) {
        this.objectMapper = objectMapper;
        this.client = client;
    }

    public BotApiMethod<?> getPlainSendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        return message;
    }

    public BotApiMethod<?> getCallbackQuery(Update update, String message) {
        CallbackQuery callbackQuery = update.getCallbackQuery();

        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
        answerCallbackQuery.setText(message);

        return answerCallbackQuery;
    }

    @Override
    @SneakyThrows
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        boolean isCommand = message != null && getCommand().equals(message.getText());

        CallbackQuery callbackQuery = update.getCallbackQuery();
        boolean isCallback = callbackQuery != null && getCommand().equals(objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class).getCommand());

        return isCommand || isCallback;
    }

    public abstract String getCommand();
}
