package ru.nesterov.bot.handlers.implementation.invocable;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.dto.GetClientStatisticResponse;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;

public class GetClientStatisticHandler extends DisplayedCommandHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        return null;
    }

    @Override
    public String getCommand() {
        return null;
    }

    @SneakyThrows
    private BotApiMethod<?> sendClientStatictic(Update update, String clientName) {
        long userId = update.getCallbackQuery().getFrom().getId();
        CallbackQuery callbackQuery = update.getCallbackQuery(); //???
        ButtonCallback callback = objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class);
        GetClientStatisticResponse response = client.getClientStatistic(userId, clientName);

        return editMessage(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                formatIncomeReport(response),
                null
        );
    }
}
