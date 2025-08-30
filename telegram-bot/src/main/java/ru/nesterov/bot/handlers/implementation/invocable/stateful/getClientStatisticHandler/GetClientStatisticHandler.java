package ru.nesterov.bot.handlers.implementation.invocable.stateful.getClientStatisticHandler;

import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.dto.GetClientStatisticRequest;
import ru.nesterov.bot.dto.GetClientStatisticResponse;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.statemachine.dto.Action;
import ru.nesterov.bot.utils.TelegramUpdateUtils;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Component
public class GetClientStatisticHandler extends StatefulCommandHandler<State, GetClientStatisticRequest> {
    public GetClientStatisticHandler() {
        super(State.STARTED, GetClientStatisticRequest.class);
    }

    @Override
    public void initTransitions() {
        stateMachineProvider
                .addTransition(State.STARTED, Action.COMMAND_INPUT, State.SELECT_CLIENT, this::sendClientNamesKeyboard)

                .addTransition(State.SELECT_CLIENT, Action.ANY_CALLBACK_INPUT, State.FINISH, this::handleClientName);
    }

    @Override
    public String getCommand() {
        return "Узнать статистику по клиенту";
    }

    @SneakyThrows
    private BotApiMethod<?> handleClientName(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
         GetClientStatisticResponse response = client.getClientStatistic(userId, buttonCallback.getValue());

        return editMessage(
                TelegramUpdateUtils.getChatId(update),
                TelegramUpdateUtils.getMessageId(update),
                formatIncomeReport(response),
                null
        );
    }

    @SneakyThrows
    private BotApiMethod<?> sendClientNamesKeyboard(Update update) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<GetActiveClientResponse> clients = client.getActiveClients(TelegramUpdateUtils.getUserId(update));

        if (clients.isEmpty()) {
            return getPlainSendMessage(TelegramUpdateUtils.getChatId(update), "Нет доступных клиентов");
        }

        clients.sort(Comparator.comparing(GetActiveClientResponse::getName, String.CASE_INSENSITIVE_ORDER));

        for (GetActiveClientResponse response : clients) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(response.getName());
            ButtonCallback callback = new ButtonCallback();
            callback.setCommand(getCommand());
            callback.setValue(response.getName());
            button.setCallbackData(buttonCallbackService.getTelegramButtonCallbackString(callback));

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            keyboard.add(rowInline);
        }
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Выберите клиента, чью статистику хотите узнать:", keyboardMarkup);
    }

    private static String formatIncomeReport(GetClientStatisticResponse response) {
        NumberFormat currencyFormat = NumberFormat.getNumberInstance(new Locale("ru", "RU"));
        currencyFormat.setMinimumFractionDigits(0);
        currencyFormat.setMaximumFractionDigits(0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru", "RU"));

        return String.format(
                "📊 *Статистика клиента*\n\n" +
                        "%-35s %s\n" +
                        "%-35s %s\n" +
                        "%-35s %s\n" +
                        "-------------------------------------------\n" +
                        "%-35s %s\n" +
                        "%-35s %s\n" +
                        "%-35s %s\n" +
                        "-------------------------------------------\n" +
                        "%-35s %s часов\n" +
                        "%-35s %s часов\n" +
                        "%-35s %s ₽/час\n" +
                        "%-35s %s\n" +
                        "%-35s %s\n" +
                        "%-35s %s\n" +
                        "-------------------------------------------\n" +
                        "%-35s %s ₽",

                "Имя:", response.getName(),
                "ID:", (response.getId()),
                "Телефон:", response.getPhone(),

                "Описание:", response.getDescription(),
                "Начало обучения:", dateFormat.format(response.getStartDate()),
                "Продолжительность обучения:", response.getServiceDuration() + " дней",

                "Состоявшихся занятий в часах:", currencyFormat.format(response.getSuccessfulMeetingsHours()),
                "Отмененных занятий в часах:", currencyFormat.format(response.getCancelledMeetingsHours()),
                "Доход в час:", currencyFormat.format(response.getIncomePerHour()),
                "Количество состоявшихся занятий:", currencyFormat.format(response.getSuccessfulEventsCount()),
                "Количество запланированно отмененных занятий:", currencyFormat.format(response.getPlannedCancelledEventsCount()),
                "Количество не запланированно отмененных занятий:", currencyFormat.format(response.getNotPlannedCancelledEventsCount()),

                "Суммарный доход:", currencyFormat.format(response.getTotalIncome())
        );
    }
}
