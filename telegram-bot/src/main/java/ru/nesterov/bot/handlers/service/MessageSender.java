package ru.nesterov.bot.handlers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.RevenueAnalyzerBot;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageSender {

    private final RevenueAnalyzerBot bot;

    public void send(BotApiMethod<?> message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка отправки сообщения", e);
        }
    }

    public SendMessage buildTextMessage(Update update, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(TelegramUpdateUtils.getChatId(update));
        message.setText(text);
        return message;
    }
}