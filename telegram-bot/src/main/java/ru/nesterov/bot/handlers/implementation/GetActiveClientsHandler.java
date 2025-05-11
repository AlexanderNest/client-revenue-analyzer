package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.dto.GetActiveClientResponse;

import java.util.List;
@Component
@RequiredArgsConstructor
public class GetActiveClientsHandler extends DisplayedCommandHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        long userId = TelegramUpdateUtils.getUserId(update);
        List<GetActiveClientResponse> activeClientResponseList =  client.getActiveClients(userId);

        String activeClients = activeClientResponseList.toString();

            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), activeClients);
    }

    @Override
    public boolean isFinished(Long userId) {
        return true;
    }

    @Override
    public String getCommand() {
        return "Вывести список клиентов";
    }
}
