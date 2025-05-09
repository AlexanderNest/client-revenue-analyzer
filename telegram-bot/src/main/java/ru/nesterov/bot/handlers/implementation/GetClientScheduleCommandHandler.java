package ru.nesterov.bot.handlers.implementation;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.abstractions.DisplayedCommandHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.dto.GetActiveClientResponse;
import ru.nesterov.dto.GetClientScheduleRequest;
import ru.nesterov.dto.GetClientScheduleResponse;

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
@RequiredArgsConstructor
public class GetClientScheduleCommandHandler extends DisplayedCommandHandler {
    private final BotHandlersRequestsKeeper handlersKeeper;
    private final InlineCalendarBuilder inlineCalendarBuilder;

    private static final String ENTER_FIRST_DATE = "Введите первую дату";
    private static final String ENTER_SECOND_DATE = "Введите вторую дату";


    @SneakyThrows
    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = getChatId(update);
        long userId = getUserId(update);

        GetClientScheduleRequest getClientScheduleRequest = handlersKeeper.getRequest(userId, GetClientScheduleCommandHandler.class, GetClientScheduleRequest.class);

        if (getClientScheduleRequest == null) {
            getClientScheduleRequest = handlersKeeper.putRequest(
                    GetClientScheduleCommandHandler.class,
                    userId,
                    GetClientScheduleRequest.builder()
                            .userId(userId)
                            .displayedMonth(LocalDate.now())
                            .build()
            );
        }

        BotApiMethod<?> message;
        if (isMessageWithText(update)) {
            getClientScheduleRequest.setClientName(null);
            getClientScheduleRequest.setDisplayedMonth(LocalDate.now());
            getClientScheduleRequest.setFirstDate(null);
            getClientScheduleRequest.setSecondDate(null);

            message = sendClientNamesKeyboard(chatId, userId);
        } else {
            message = handleCallbackQuery(update, getClientScheduleRequest);
        }
        return message;
    }

    @Override
    public boolean isFinished(Long userId) {
        GetClientScheduleRequest request = handlersKeeper.getRequest(userId, GetClientScheduleCommandHandler.class, GetClientScheduleRequest.class);
        return request == null || request.isFilled();
    }

    @Override
    public String getCommand() {
        return "Узнать расписание клиента";
    }

    @SneakyThrows
    private BotApiMethod<?> sendClientSchedule(long chatId, int messageId, GetClientScheduleRequest getClientScheduleRequest) {
        List<GetClientScheduleResponse> response = client.getClientSchedule(
                getClientScheduleRequest.getUserId(),
                getClientScheduleRequest.getClientName(),
                getClientScheduleRequest.getFirstDate().atStartOfDay(),
                getClientScheduleRequest.getSecondDate().atStartOfDay());

        return editMessage(chatId, messageId, formatClientSchedule(response), null);
    }

    @SneakyThrows
    private BotApiMethod<?> sendClientNamesKeyboard(long chatId, long userId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<GetActiveClientResponse> clients = client.getActiveClients(userId);

        if (clients.isEmpty()) {
            return getPlainSendMessage(chatId, "Нет доступных клиентов");
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

        return getReplyKeyboard(chatId, "Выберите клиента для которого хотите получить расписание:", keyboardMarkup);
    }

    @SneakyThrows
    private BotApiMethod<?> handleCallbackQuery(Update update, GetClientScheduleRequest getClientScheduleRequest) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();
        ButtonCallback callback = buttonCallbackService.buildButtonCallback(callbackData);

        if (isValidDate(callback.getValue())) {
            return handleSelectedDate(callback.getValue(),
                    callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId(),
                    getClientScheduleRequest);
        } else {
            return switch (callback.getValue()) {
                case "Next", "Prev" -> handleMonthSwitch(
                        callback.getValue(),
                        callbackQuery.getMessage().getChatId(),
                        callbackQuery.getMessage().getMessageId(),
                        getClientScheduleRequest);
                default -> handleClientName(
                        callbackQuery.getMessage().getChatId(),
                        callbackQuery.getMessage().getMessageId(),
                        getClientScheduleRequest,
                        callback.getValue()
                );
            };
        }
    }

    private BotApiMethod<?> handleClientName(long chatId, int messageId, GetClientScheduleRequest getClientScheduleRequest, String clientName) {
        if (getClientScheduleRequest.getClientName() == null) {
            getClientScheduleRequest.setClientName(clientName);
            return sendCalendarKeyBoard(chatId, messageId, ENTER_FIRST_DATE, getClientScheduleRequest.getDisplayedMonth());
        }
        return null;
    }

    @SneakyThrows
    private BotApiMethod<?> handleMonthSwitch(String callbackValue,
                                              long callbackQueryChatId,
                                              int callbackQueryMessageId,
                                              GetClientScheduleRequest getClientScheduleRequest) {
        if ("Next".equals(callbackValue)) {
            getClientScheduleRequest.setDisplayedMonth(getClientScheduleRequest.getDisplayedMonth().plusMonths(1));
        } else if ("Prev".equals(callbackValue)) {
            getClientScheduleRequest.setDisplayedMonth(getClientScheduleRequest.getDisplayedMonth().minusMonths(1));
        }

        String calendarMessage = "";
        if (getClientScheduleRequest.getFirstDate() == null) {
            calendarMessage = ENTER_FIRST_DATE;
        } else if (getClientScheduleRequest.getSecondDate() == null) {
            calendarMessage = ENTER_SECOND_DATE;
        }

        return sendCalendarKeyBoard(
                callbackQueryChatId,
                callbackQueryMessageId,
                calendarMessage,
                getClientScheduleRequest.getDisplayedMonth()
        );
    }

    @SneakyThrows
    private BotApiMethod<?> handleSelectedDate(String callbackValue,
                                               long callbackQueryChatId,
                                               int callbackQueryMessageId,
                                               GetClientScheduleRequest getClientScheduleRequest) {
        if (getClientScheduleRequest.getFirstDate() == null) {
            getClientScheduleRequest.setFirstDate(LocalDate.parse(callbackValue));
            return sendCalendarKeyBoard(
                    callbackQueryChatId,
                    callbackQueryMessageId,
                    ENTER_SECOND_DATE,
                    getClientScheduleRequest.getDisplayedMonth()
            );
        } else {
            getClientScheduleRequest.setSecondDate(LocalDate.parse(callbackValue));
            return sendClientSchedule(
                    callbackQueryChatId,
                    callbackQueryMessageId,
                    getClientScheduleRequest);
        }
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

    private boolean isValidDate(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private Long getChatId(Update update) {
        return update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
    }

    private Long getUserId(Update update) {
        return update.hasMessage() ? update.getMessage().getFrom().getId() : update.getCallbackQuery().getFrom().getId();
    }

    private boolean isMessageWithText(Update update) {
        return update.getMessage() != null && update.getMessage().hasText();
    }

    @Override
    public int getOrder() {
        return 6;
    }


    //////////////////

    @SneakyThrows
    private BotApiMethod<?> sendCalendarKeyBoard(long chatId, int messageId, String text, LocalDate date) {
        return editMessage(
                chatId,
                messageId,
                text,
                inlineCalendarBuilder.createCalendarMarkup(date, getCommand(), buttonCallbackService)
        );
    }
}
