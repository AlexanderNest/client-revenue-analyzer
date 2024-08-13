package ru.nesterov.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.handlers.CommandHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RevenueAnalyzerBot extends TelegramLongPollingBot {
    private final ClientRevenueAnalyzerIntegrationClient client;
    private final List<CommandHandler> commandHandlers;

    public RevenueAnalyzerBot(ClientRevenueAnalyzerIntegrationClient client, List<CommandHandler> commandHandlers) {
        super("7377383101:AAGq1kkEcnqsL1xkUN-u4A4SxMKlCEQ1cv4");
        this.client = client;
        this.commandHandlers = commandHandlers;
    }

    @Override
    public void onUpdateReceived(Update update) {
        chooseHandler(update);
    }

    @Override
    public String getBotUsername() {
        return "analyzer bot";
    }


    private void chooseHandler(Update update) {
        for (CommandHandler commandHandler : commandHandlers) {
            if (commandHandler.isApplicable(update)) {
                BotApiMethod<?> sendMessage = commandHandler.handle(update);
                sendMessage(sendMessage);
            }
        }
    }

    private void sendMessage(BotApiMethod<?> message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }
}
