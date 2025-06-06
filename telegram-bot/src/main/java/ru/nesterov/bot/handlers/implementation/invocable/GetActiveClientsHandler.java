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

@Component
@RequiredArgsConstructor
public class GetActiveClientsHandler extends DisplayedCommandHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        List<GetActiveClientResponse> activeClientResponseList =  client.getActiveClients(userId);

        String activeClients = activeClientResponseList.stream()
                .map(activeClientResponse -> String.format(
                        "\uD83D\uDC71 Имя: %s, \uD83D\uDCB2 Стоимость за час: %d, \uD83D\uDCD1 Описание: %s",
                        activeClientResponse.getName(),
                        activeClientResponse.getPricePerHour(),
                        activeClientResponse.getDescription()
                ))
                .collect(Collectors.joining("\n"));

            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), activeClients);
    }

    @Override
    public String getCommand() {
        return "Вывести список клиентов";
    }
}
