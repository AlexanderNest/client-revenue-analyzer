package ru.nesterov.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.service.HandlersService;
import ru.nesterov.integration.ErrorMessage;
import ru.nesterov.properties.BotProperties;

@Service
@Slf4j
@ConditionalOnProperty("bot.enabled")
public class RevenueAnalyzerBot extends TelegramLongPollingBot {
    private final HandlersService handlersService;
    private final BotProperties botProperties;

    public RevenueAnalyzerBot(BotProperties botProperties, HandlersService handlersService) {
        super(botProperties.getApiToken());
        this.handlersService = handlersService;
        this.botProperties = botProperties;
    }

    @Override
    public void onUpdateReceived(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler commandHandler = handlersService.getHandler(update);
        if (commandHandler == null) {
            log.error("Не удалось обработать сообщение");
            return;
        }

        log.debug("Выбранный CommandHandler = {}", commandHandler.getClass().getSimpleName());

        BotApiMethod<?> sendMessage = null;

        try {
            sendMessage = commandHandler.handle(update);
        } catch (Throwable e) {
            log.error("Произошла ошибка при обработке обновления: {}", e.getMessage());
            ErrorMessage error = new ErrorMessage(e.getClass().getSimpleName(), e.getMessage());

            BotApiMethod<?> sendErrorMessage = buildSendMessage(userId, error.getMessage(), null);
            try {
                execute(sendErrorMessage);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (commandHandler.isFinished(userId)) {
                handlersService.resetHandlers(userId);
            }
        }

        if (sendMessage != null) {
            try {
                executeAsync(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения: {}", e.getMessage());
            }
        }
    }

    private BotApiMethod<?> buildSendMessage(long chatId, String text, ReplyKeyboard replyKeyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setReplyMarkup(replyKeyboard);
        return message;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}