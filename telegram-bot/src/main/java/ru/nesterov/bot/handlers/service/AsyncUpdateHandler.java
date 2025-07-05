package ru.nesterov.bot.handlers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.RevenueAnalyzerBot;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncUpdateHandler {

    private final HandlersService handlersService;
    private final MessageSender messageSender;

    @Async("botTaskExecutor") // имя Executor-а из AsyncConfig
    public void handle(Update update) {
        log.info("🔄 Обработка update {} в потоке: {}", update.getUpdateId(), Thread.currentThread().getName());

        try {
            Thread.sleep(5000); // тестовая задержка, чтобы потоки пересеклись
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long userId = TelegramUpdateUtils.getUserId(update);

        CommandHandler commandHandler = handlersService.getHandler(update);
        if (commandHandler == null) {
            log.error("Не удалось обработать сообщение");
            return;
        }

        log.debug("Выбранный CommandHandler = {}", commandHandler.getClass().getSimpleName());
        log.debug("Обработка в потоке: {}", Thread.currentThread().getName());

        BotApiMethod<?> sendMessage;

        try {
            sendMessage = commandHandler.handle(update);
        } catch (Exception exception) {
            handlersService.resetBrokeHandler(commandHandler, userId);
            sendMessage = messageSender.buildTextMessage(update, exception.getMessage());
        } finally {
            handlersService.resetFinishedHandlers(userId);
        }

        messageSender.send(sendMessage);
    }
}