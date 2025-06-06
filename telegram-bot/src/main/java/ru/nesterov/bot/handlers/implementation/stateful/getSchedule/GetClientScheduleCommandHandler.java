package ru.nesterov.bot.handlers.implementation.stateful.getSchedule;


import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.dto.GetActiveClientResponse;
import ru.nesterov.dto.GetClientScheduleRequest;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.statemachine.dto.Action;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Получение информации о расписании указанного клиента
 */

@Component
public class GetClientScheduleCommandHandler extends StatefulCommandHandler<State, GetClientScheduleRequest> {

    private final InlineCalendarBuilder inlineCalendarBuilder;

    private static final String ENTER_FIRST_DATE = "Введите первую дату";
    private static final String ENTER_SECOND_DATE = "Введите вторую дату";

    public GetClientScheduleCommandHandler(InlineCalendarBuilder inlineCalendarBuilder) {
        super(State.STARTED, GetClientScheduleRequest.class);
        this.inlineCalendarBuilder = inlineCalendarBuilder;
    }

    @Override
    public void initTransitions() {
        stateMachineProvider
                .addTransition(State.STARTED, Action.COMMAND_INPUT, State.SELECT_CLIENT, this::sendClientNamesKeyboard)
                .addTransition(State.SELECT_CLIENT, Action.ANY_CALLBACK_INPUT, State.SELECT_FIRST_DATE, this::handleClientName)
                .addTransition(State.SELECT_FIRST_DATE, Action.ANY_CALLBACK_INPUT, State.SELECT_SECOND_DATE, this::handleFirstDate)
                .addTransition(State.SELECT_SECOND_DATE, Action.ANY_CALLBACK_INPUT, State.FINISH, this::handleSecondDate);
//                .addTransition(State.SECOND_DATE_SELECTED, Action.ANY_CALLBACK_INPUT, State.FINISH, this::sendClientSchedule);
    }

    private BotApiMethod<?> handleClientName(Update update) {
        if (getStateMachine(update).getMemory().getClientName() == null) {
            ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());
            getStateMachine(update).getMemory().setClientName(buttonCallback.getValue());
        }

        return sendCalendarKeyBoard(update, ENTER_FIRST_DATE, LocalDate.now());
    }

    @SneakyThrows
    private BotApiMethod<?> handleFirstDate(Update update) {
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());

        getStateMachine(update).getMemory().setFirstDate(LocalDate.parse(buttonCallback.getValue()));
        return sendCalendarKeyBoard(update, ENTER_SECOND_DATE, getStateMachine(update).getMemory().getFirstDate());
    }

    @SneakyThrows
    private BotApiMethod<?> handleSecondDate(Update update) {
        ButtonCallback buttonCallback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());

        getStateMachine(update).getMemory().setSecondDate(LocalDate.parse(buttonCallback.getValue()));
        return sendClientSchedule(update);
    }

    @SneakyThrows
    private BotApiMethod<?> sendCalendarKeyBoard(Update update, String text, LocalDate date) {
        return editMessage(TelegramUpdateUtils.getChatId(update),
                TelegramUpdateUtils.getMessageId(update),
                text,
                inlineCalendarBuilder.createCalendarMarkup(date, getCommand(), buttonCallbackService)
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

        return getReplyKeyboard(TelegramUpdateUtils.getChatId(update), "Выберите клиента для которого хотите получить расписание:", keyboardMarkup);
    }

    @SneakyThrows
    private BotApiMethod<?> sendClientSchedule(Update update) {
        List<GetClientScheduleResponse> response = client.getClientSchedule(
                TelegramUpdateUtils.getUserId(update),
                getStateMachine(update).getMemory().getClientName(),
                getStateMachine(update).getMemory().getFirstDate().atStartOfDay(),
                getStateMachine(update).getMemory().getSecondDate().atStartOfDay());

        return editMessage(TelegramUpdateUtils.getChatId(update),
                TelegramUpdateUtils.getMessageId(update),
                formatClientSchedule(response), null);
    }

    private String formatClientSchedule(List<GetClientScheduleResponse> response) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"));

        if (response.isEmpty()) {
            return "📅 Встречи не запланированы";
        }

        return response.stream()
                .map(schedule -> {
                    String startDate = schedule.getEventStart().format(dateFormatter);
                    String startTime = schedule.getEventStart().format(timeFormatter);
                    String endTime = schedule.getEventEnd().format(timeFormatter);

                    return String.format(
                            "📅 Дата: %s\n⏰ Время: %s - %s",
                            startDate, startTime, endTime);
                })
                .collect(Collectors.joining("\n\n"));
    }

    @SneakyThrows
    private BotApiMethod<?> handleCallbackQuery(Update update) {
        ButtonCallback callback = buttonCallbackService.buildButtonCallback(update.getCallbackQuery().getData());

        if (isValidDate(callback.getValue())) {
            return handleFirstDate(update);
        } else {
            return switch (callback.getValue()) {
                case "Next", "Prev" -> handleMonthSwitch(update);
                default -> handleClientName(update);
            };
        }
    }

    private boolean isValidDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @SneakyThrows
    private BotApiMethod<?> handleMonthSwitch(Update update) {
        if ("Next".equals(update.getCallbackQuery().getData())) {
            getStateMachine(update)
                    .getMemory()
                    .setDisplayedMonth(getStateMachine(update)
                            .getMemory()
                            .getDisplayedMonth()
                            .plusMonths(1));
        } else if ("Prev".equals(update.getCallbackQuery().getData())) {
            getStateMachine(update)
                    .getMemory()
                    .setDisplayedMonth(getStateMachine(update)
                            .getMemory()
                            .getDisplayedMonth()
                            .minusMonths(1));
        }

        String calendarMessage = "";
        if (getStateMachine(update).getMemory().getFirstDate() == null) {
            calendarMessage = ENTER_FIRST_DATE;
        } else if (getStateMachine(update).getMemory().getSecondDate() == null) {
            calendarMessage = ENTER_SECOND_DATE;
        }

        return sendCalendarKeyBoard(update, calendarMessage, getStateMachine(update).getMemory().getDisplayedMonth());
    }

    @Override
    public int getOrder() {
        return 6;
    }

    @Override
    public String getCommand() {
        return "Узнать расписание клиента";
    }
}

