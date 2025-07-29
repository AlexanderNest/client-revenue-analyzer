package ru.nesterov.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.config.BotProperties;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.service.HandlersService;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

@Service
@Slf4j
@ConditionalOnProperty("bot.enabled")
public class RevenueAnalyzerBot extends TelegramLongPollingBot {
    private final HandlersService handlersService;
    private final BotProperties botProperties;
    private final TaskExecutor taskExecutor;


    public RevenueAnalyzerBot(BotProperties botProperties, HandlersService handlersService,
                              @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor) {
        super(botProperties.getApiToken());
        this.handlersService = handlersService;
        this.botProperties = botProperties;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void onUpdateReceived(Update update) {
        taskExecutor.execute(() -> handleUpdate(update));
    }

    public void handleUpdate(Update update) {

        long chatId = TelegramUpdateUtils.getChatId(update);

        CommandHandler commandHandler = handlersService.getHandler(update);
        if (commandHandler == null) {
            log.error("Не удалось обработать сообщение");
            return;
        }

        log.debug("Выбранный CommandHandler = {}", commandHandler.getClass().getSimpleName());

        BotApiMethod<?> sendMessage;

        try {
            sendMessage = commandHandler.handle(update);
        } catch (Exception exception) {
            handlersService.resetBrokeHandler(commandHandler, chatId);
            sendMessage = buildTextMessage(update, exception.getMessage());
        } finally {
            handlersService.resetFinishedHandlers(chatId);
        }
        sendMessage(sendMessage);
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    private SendMessage buildTextMessage(Update update, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(TelegramUpdateUtils.getChatId(update));
        message.setText(text);

        return message;
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}