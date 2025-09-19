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
    private static String clientStatisticTemplate;
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
    private List<BotApiMethod<?>> handleClientName(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
        GetClientStatisticResponse response = client.getClientStatistic(userId, buttonCallback.getValue());

         if(response == null) {
             return getPlainSendMessage(
                     TelegramUpdateUtils.getChatId(update),
                     "У пользователя нет встреч"
             );
         }

        return editMessage(
                TelegramUpdateUtils.getChatId(update),
                TelegramUpdateUtils.getMessageId(update),
                formatIncomeReport(response),
                null
        );
    }

    @SneakyThrows
    private List<BotApiMethod<?>> sendClientNamesKeyboard(Update update) {
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

        String successfulHours = String.format("%s часов", currencyFormat.format(response.getSuccessfulMeetingsHours()));
        String cancelledHours = String.format("%s часов", currencyFormat.format(response.getCancelledMeetingsHours()));
        String incomePerHour = String.format("%s ₽/час", currencyFormat.format(response.getIncomePerHour()));
        String successfulEvents = String.format("%s", currencyFormat.format(response.getSuccessfulEventsCount()));
        String plannedCancelled = String.format("%s", currencyFormat.format(response.getPlannedCancelledEventsCount()));
        String notPlannedCancelled = String.format("%s", currencyFormat.format(response.getNotPlannedCancelledEventsCount()));
        String totalIncome = String.format("%s ₽", currencyFormat.format(response.getTotalIncome()));

        return "📊 *Статистика клиента*\n\n" +
                String.format("%s %70s", "Имя:", response.getName()) + "\n" +
                String.format("%s %72s", "ID:", response.getId()) + "\n" +
                String.format("%s %60s", "Телефон:", response.getPhone()) + "\n" +
                "─────────────────────────────────────────" + "\n" +
                String.format("%s %65s", "Описание:", response.getDescription()) + "\n" +
                String.format("%s %48s", "Начало обучения:", dateFormat.format(response.getStartDate())) + "\n" +
                String.format("%s %17s", "Продолжительность обучения:", response.getServiceDuration() + " дней") + "\n" +
                "─────────────────────────────────────────" + "\n" +
                String.format("%s %34s", "Состоявшихся занятий:", successfulHours) + "\n" +
                String.format("%s %37s", "Отмененных занятий:", cancelledHours) + "\n" +
                String.format("%s %59s", "Доход в час:", incomePerHour) + "\n" +
                String.format("%s %28s", "Состоявшиеся занятия:", successfulEvents) + "\n" +
                String.format("%s %19s", "Запланированные отмены:", plannedCancelled) + "\n" +
                String.format("%s %14s", "Незапланированные отмены:", notPlannedCancelled) + "\n" +
                "─────────────────────────────────────────" + "\n" +
                String.format("%s %40s", "Суммарный доход:", totalIncome) + "\n";
    }
}
