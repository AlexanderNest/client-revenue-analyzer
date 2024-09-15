package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.CommandHandler;
import ru.nesterov.bot.handlers.callback.CalendarIntegrationCallback;
import ru.nesterov.google.GoogleCalendarClient;
import ru.nesterov.google.GoogleCalendarProperties;
import ru.nesterov.google.GoogleCalendarService;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("bot.enabled")
public class CalendarIntegrationHandler implements CommandHandler {
    private final ObjectMapper objectMapper;
    private final ClientRevenueAnalyzerIntegrationClient client;

    @Bean(name = "userGoogleCalendarClient")
    public GoogleCalendarClient userGoogleCalendarClient() {
        return new
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        BotApiMethod<?> sendMessage;
        if (update.getMessage() == null) {

        }
        return null;
    }

    @Override
    public boolean isApplicable(Update update) {
        Message message = update.getMessage();
        boolean isCommand = message != null && "/addcalendar".equals(message.getText());

        CallbackQuery callbackQuery = update.getCallbackQuery();
        boolean isCallback = callbackQuery != null && "/addcalendar".equals(objectMapper.readValue(callbackQuery.getData(), CalendarIntegrationCallback.class))
        return false;
    }
}
