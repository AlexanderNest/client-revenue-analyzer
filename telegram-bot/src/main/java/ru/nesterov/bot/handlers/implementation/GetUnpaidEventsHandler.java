package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.dto.EventDto;
import ru.nesterov.dto.EventResponse;
import ru.nesterov.dto.GetUnpaidAnalyzerRequest;
import ru.nesterov.dto.GetUnpaidEventsResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.repository.UserRepository;
import ru.nesterov.service.dto.UserDto;
import ru.nesterov.service.event.EventsAnalyzerServiceImpl;
import ru.nesterov.service.user.UserServiceImpl;

import java.util.List;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty("bot.enabled")
public class GetUnpaidEventsHandler extends DisplayedCommandHandler {
    // доработка тут. нужно получить ответ и тут отформатировать его в строку. отправить красивый ответ в бота

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

        /*
         * List<Event> events = client.getUpdaindEvents(userId);
         * String message = formatMessage(events);
         * sendMessage(message);
         */

    }

    private String formatMessage(List<EventResponse> events) {
        StringBuilder message = new StringBuilder("Неоплаченные события:\n");
        for(EventResponse event: events) {
            message.append("- ").append(event.getSummary())
                    .append(" (").append(event.getEventStart()).append(")\n"); //
        }
        return message.toString();
    }





    @Override
    public boolean isFinished(Long userId) {
        return true;
    }


}
