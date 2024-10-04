package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.dto.GetActiveClientResponse;
import ru.nesterov.dto.GetClientScheduleRequest;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty("bot.enabled")
public class GetClientScheduleHandler extends ClientRevenueAbstractHandler {
    private final BotHandlersRequestsKeeper handlersKeeper;
    private final InlineCalendarBuilder inlineCalendarBuilder;

    private static final String ENTER_FIRST_DATE = "Введите первую дату";
    private static final String ENTER_SECOND_DATE = "Введите вторую дату";

    public GetClientScheduleHandler(ObjectMapper objectMapper, ClientRevenueAnalyzerIntegrationClient client,
                                    BotHandlersRequestsKeeper handlersKeeper, InlineCalendarBuilder inlineCalendarBuilder) {
        super(objectMapper, client);
        this.handlersKeeper = handlersKeeper;
        this.inlineCalendarBuilder = inlineCalendarBuilder;
    }

    @SneakyThrows
    @Override
    public BotApiMethod<?> handle(Update update) {
        long chatId = getChatId(update);
        long userId = getUserId(update);

        GetClientScheduleRequest getClientScheduleRequest = handlersKeeper.getRequest(userId, GetClientScheduleHandler.class, GetClientScheduleRequest.class);

        if (getClientScheduleRequest == null) {
            getClientScheduleRequest = handlersKeeper.putRequest(
                    GetClientScheduleHandler.class,
                    userId,
                    GetClientScheduleRequest.builder()
                            .userId(userId)
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

    @SneakyThrows
    private BotApiMethod<?> handleCallbackQuery(Update update, GetClientScheduleRequest getClientScheduleRequest) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();
        ButtonCallback callback;
        try {
            callback = objectMapper.readValue(callbackData, ButtonCallback.class);
        } catch (JsonProcessingException e) {
            callback = null;
        }
        if (callback == null) {
            callback = ButtonCallback.fromShortString(callbackData);
        }

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

    @Override
    public String getCommand() {
        return "/clientschedule";
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

        for (GetActiveClientResponse response : clients) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(response.getName());
            String callbackData = getCommand() + ":" + response.getName();
            button.setCallbackData(ButtonCallback.fromShortString(callbackData).toShortString());

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            keyboard.add(rowInline);
        }
        keyboardMarkup.setKeyboard(keyboard);

        return getReplyKeyboard(chatId, "Выберите клиента для которого хотите получить расписание:", keyboardMarkup);
    }

    @SneakyThrows
    private BotApiMethod<?> sendCalendarKeyBoard(long chatId, int messageId, String text, LocalDate date) {
        return editMessage(
                chatId,
                messageId,
                text,
                inlineCalendarBuilder.createCalendarMarkup(date, getCommand())
        );
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
        if (callbackValue.equals("Next")) {
            getClientScheduleRequest.setDisplayedMonth(getClientScheduleRequest.getDisplayedMonth().plusMonths(1));
        } else if (callbackValue.equals("Prev")) {
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
            return "📅 Расписание отсутствует";
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
}