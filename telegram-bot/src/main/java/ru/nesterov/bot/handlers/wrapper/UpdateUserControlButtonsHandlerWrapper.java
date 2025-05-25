package ru.nesterov.bot.handlers.wrapper;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.implementation.UpdateUserControlButtonsHandler;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class UpdateUserControlButtonsHandlerWrapper {
    private final UpdateUserControlButtonsHandler updateUserControlButtonsHandler;
    private final Map<Long, LocalDateTime> timeMap = new HashMap<>();

    @Value("${bot.update.time}")
    private Long timeIntervalInSeconds;

    public ReplyKeyboardMarkup getUpdateTime(Update update) {
        Long chatId = TelegramUpdateUtils.getChatId(update);

        if(timeMap.get(chatId) != null) {
            boolean timeInterval = Duration.between(timeMap.get(chatId), (LocalDateTime.now()))
                     .abs()
                     .getSeconds() > timeIntervalInSeconds;
            if(timeInterval) {
                return updateUserControlButtonsHandler.getReplyKeyboardMarkup(update);
            }
        } else {
            timeMap.put(chatId, LocalDateTime.now());
        }

        return null;
    }
}
