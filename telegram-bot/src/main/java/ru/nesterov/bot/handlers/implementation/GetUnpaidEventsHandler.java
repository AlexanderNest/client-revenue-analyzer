package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.dto.EventResponse;
import ru.nesterov.dto.GetUnpaidEventsResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.util.List;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty("bot.enabled")
public class GetUnpaidEventsHandler extends DisplayedCommandHandler {
    private final ClientRevenueAnalyzerIntegrationClient client;

    @Override
    public String getCommand() {
        return "Узнать неоплаченные события";
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        long chatId = TelegramUpdateUtils.getChatId(update);

        GetUnpaidEventsResponse getUnpaidEventsResponse = client.getUnpaidEvents(userId);
        List<EventResponse> unpaidEvents = getUnpaidEventsResponse.getEvents();

        if (unpaidEvents.isEmpty()) {
            return getPlainSendMessage(chatId, "Нет неоплаченных событий");
        }

        String message = formatMessage(unpaidEvents);
        return getPlainSendMessage(chatId, message);
    }

    private String formatMessage(List<EventResponse> events) {
        StringBuilder message = new StringBuilder("Неоплаченные события:\n");
        for(EventResponse event : events) {
            message
                    .append("- ")
                    .append(event.getSummary())
                    .append(" (")
                    .append(event.getEventStart())
                    .append(")\n");
        }

        return message.toString();
    }

    @Override
    public boolean isFinished(Long userId) {
        return true;
    }
}
