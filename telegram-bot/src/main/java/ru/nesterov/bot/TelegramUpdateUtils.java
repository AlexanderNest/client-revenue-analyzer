package ru.nesterov.bot;

import org.telegram.telegrambots.meta.api.objects.Update;

public class TelegramUpdateUtils {
    public static long getUserId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getFrom().getId();
        } else {
            return update.getCallbackQuery().getFrom().getId();
        }
    }
}
