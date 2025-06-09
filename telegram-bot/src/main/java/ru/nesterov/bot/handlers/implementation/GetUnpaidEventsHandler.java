package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.dto.GetUnpaidEventsResponse;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RequiredArgsConstructor
@Component
public class GetUnpaidEventsHandler extends DisplayedCommandHandler {

    @Override
    public String getCommand() {
        return "Узнать неоплаченные события";
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }

    @Override
    public boolean isDisplayedForRegistered() {
        return super.isDisplayedForRegistered();
    }

    @Override
    public BotApiMethod<?> handle(Update update) {

        List<GetUnpaidEventsResponse> unpaidEvents = client.getUnpaidEvents(TelegramUpdateUtils.getUserId(update));

        if (unpaidEvents.isEmpty()) {
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Нет неоплаченных событий");
        }
        String message = formatMessage(unpaidEvents);
        return getPlainSendMessage(TelegramUpdateUtils.getUserId(update), message);
    }

    private String formatMessage(List<GetUnpaidEventsResponse> events) {
        StringBuilder message = new StringBuilder("Неоплаченные события:\n");
        for(GetUnpaidEventsResponse event : events) {
            message
                    .append("- ")
                    .append(event.getSummary())
                    .append(" (")
                    .append(event.getEventStart().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                    .append(")\n");
        }
        return message.toString();
    }
}
