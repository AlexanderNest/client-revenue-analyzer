package ru.nesterov.bot.handlers.implementation.invocable;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class GetActiveClientsHandler extends DisplayedCommandHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        List<GetActiveClientResponse> activeClientResponseList = client.getActiveClients(userId);

        if (activeClientResponseList.isEmpty()) {
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update),
                    "ℹ️ У вас пока нет клиентов.");
        }

        String activeClients = IntStream.range(0, activeClientResponseList.size())
                .mapToObj(i -> {
                    GetActiveClientResponse client = activeClientResponseList.get(i);
                    return String.format(
                                    "%d. %s %n" +
                                    "     Тариф: %d руб/час %n" +
                                    "     Описание: %s %n",
                            i + 1,
                            client.getName(),
                            client.getPricePerHour(),
                            client.getDescription()
                    );
                })
                .collect(Collectors.joining("\n"));

        return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), activeClients);
    }

    @Override
    public String getCommand() {
        return "Вывести список клиентов";
    }
}
