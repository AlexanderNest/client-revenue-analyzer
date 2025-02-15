package ru.nesterov.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.service.HandlersService;
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

    private String getErrorMessage(Exception e) {
        String errorMessage = e.getMessage();

        errorMessage = errorMessage.replaceAll("^\\d+\\s+", "").replaceAll("\\{|\\}|\"|\\s*:\\s*|message", "");
        errorMessage = errorMessage.trim();

        return errorMessage;
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

        BotApiMethod<?> botApiMethod;

        try {
            botApiMethod = commandHandler.handle(update);
        } catch (Exception exception) {
            String errorMessage = getErrorMessage(exception);
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(TelegramUpdateUtils.getChatId(update));
            sendMessage.setText(errorMessage);
            botApiMethod = sendMessage;
        } finally {
            if (commandHandler.isFinished(userId)) {
                handlersService.resetHandlers(userId);
            }
        }

        sendMessage(botApiMethod);
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