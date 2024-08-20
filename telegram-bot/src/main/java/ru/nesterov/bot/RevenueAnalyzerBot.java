package ru.nesterov.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.handlers.CommandHandler;
import ru.nesterov.bot.handlers.HandlersService;

@Service
@Slf4j
public class RevenueAnalyzerBot extends TelegramLongPollingBot {
    private final ClientRevenueAnalyzerIntegrationClient client;

    private final HandlersService handlersService;

    public RevenueAnalyzerBot(ClientRevenueAnalyzerIntegrationClient client, HandlersService handlersService) {
        super("7377383101:AAGq1kkEcnqsL1xkUN-u4A4SxMKlCEQ1cv4");
        this.client = client;
        this.handlersService = handlersService;
    }

    @Override
    public void onUpdateReceived(Update update) {
        CommandHandler commandHandler = handlersService.getHandler(update);
        if (commandHandler == null) {
            log.error("Не удалось обраборать сообщение");
            return;
        }

        BotApiMethod<?> sendMessage = commandHandler.handle(update);
        sendMessage(sendMessage);
    }

    @Override
    public String getBotUsername() {
        return "analyzer bot";
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}
