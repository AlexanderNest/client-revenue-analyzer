package ru.nesterov.bot.handlers.implementation.invocable;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.dto.GetClientStatisticResponse;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;

import java.text.NumberFormat;
import java.util.Locale;

public class GetClientStatisticHandler extends DisplayedCommandHandler {
    @Override
    public BotApiMethod<?> handle(Update update) {
        BotApiMethod<?> sendMessage;
        if (update.getMessage() == null) {
            sendMessage = sendClientStatistic(update);
        } else {
            sendMessage = sendMonthKeyboard(update.getMessage().getChatId());
        }

        return sendMessage;
    }

    @Override
    public String getCommand() {
        return "Узнать статистику по клиенту";
    }

    @SneakyThrows
    private BotApiMethod<?> sendClientStatistic(Update update, String clientName) {
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

    private static String formatIncomeReport(GetClientStatisticResponse response) {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);

        return String.format(
                "📊 *Статистика клиента*\n\n" +
                        "%-22s %10s ₽\n" +
                        "%-22s %10s ₽\n" +
                        "-----------------------------\n" +
                        "%-22s %10s ₽\n" +
                        "-----------------------------\n" +
                        "%-22s %10s ₽\n" +
                        "%-22s %10s ₽",
                "Имя:", currencyFormat.format(response.getName()),
                "ID:", currencyFormat.format(response.getId()),
                "Описание:", currencyFormat.format(response.getDescription()),
                "Начало обучения:", currencyFormat.format(response.getStartDate()),
                "Продолжительность обучения:", currencyFormat.format(response.getServiceDuration()),
                "Телефон:", currencyFormat.format(response.getPhone()),
                "Состоявшихся занятий в часах:", currencyFormat.format(response.getSuccessfulMeetingsHours()),
                "Отмененных занятий в часах:", currencyFormat.format(response.getCancelledMeetingsHours()),
                "Доход в час:", currencyFormat.format(response.getIncomePerHour()),
                "Количество состоявшихся занятий:", currencyFormat.format(response.getSuccessfulEventsCount()),
                "Количество запланированно отмененных занятия:", currencyFormat.format(response.getPlannedCancelledEventsCount()),
                "Количество не запланированно отмененных занятия:", currencyFormat.format(response.getNotPlannedCancelledEventsCount()),
                "Сумарный дохожд:", currencyFormat.format(response.getTotalIncome())
        );
    }
}
