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
import ru.nesterov.bot.metric.MetricsService;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@ConditionalOnProperty("bot.enabled")
public class RevenueAnalyzerBot extends TelegramLongPollingBot {
    private final HandlersService handlersService;
    private final BotProperties botProperties;
    private final TaskExecutor taskExecutor;
    private final MetricsService metricsService;


    public RevenueAnalyzerBot(BotProperties botProperties, HandlersService handlersService,
                              @Qualifier("applicationTaskExecutor") TaskExecutor taskExecutor, MetricsService metricsService) {
        super(botProperties.getApiToken());
        this.handlersService = handlersService;
        this.botProperties = botProperties;
        this.taskExecutor = taskExecutor;
        this.metricsService = metricsService;
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

        String handlerName = commandHandler.getClass().getSimpleName();
        log.debug("Выбранный CommandHandler = {}", commandHandler.getClass().getSimpleName());

        List<BotApiMethod<?>> sendMessage;
        long start = System.nanoTime();

        try {
            sendMessage = commandHandler.handle(update);
            metricsService.recordHandlerCall(
                    handlerName,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start),
                    true,
                    null
            );
        } catch (Exception exception) {
            metricsService.recordHandlerCall(
                    handlerName,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start),
                    false,
                    exception
            );
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

    private List<BotApiMethod<?>> buildTextMessage(Update update, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(TelegramUpdateUtils.getChatId(update));
        message.setText(text);

        return List.of(message);
    }

    private void sendMessage(List<BotApiMethod<?>> message) {
        for (BotApiMethod<?> thisMessage : message) {
            try {
                execute(thisMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка отправки сообщения", e);
            }
        }
    }
}