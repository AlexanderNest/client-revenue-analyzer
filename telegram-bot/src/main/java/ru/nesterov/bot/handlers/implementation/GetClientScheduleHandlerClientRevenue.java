package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.ClientResponse;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.session.BotSessionManager;
import ru.nesterov.session.UserSession;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static ru.nesterov.session.SessionState.AWAITING_FIRST_DATE;
import static ru.nesterov.session.SessionState.AWAITING_NAME;
import static ru.nesterov.session.SessionState.AWAITING_SECOND_DATE;
import static ru.nesterov.session.SessionState.COMPLETED;

@Component
@ConditionalOnProperty("bot.enabled")
public class GetClientScheduleHandlerClientRevenue extends ClientRevenueAbstractHandler {
    private final BotSessionManager sessionManager;

    public GetClientScheduleHandlerClientRevenue(ObjectMapper objectMapper, ClientRevenueAnalyzerIntegrationClient client, BotSessionManager sessionManager) {
        super(objectMapper, client);
        this.sessionManager = sessionManager;
    }

    @Override
    public List<BotApiMethod<?>> handle(Update update) {
        Long chatId = getChatId(update);
        UserSession session = sessionManager.getSession(chatId);
        List<BotApiMethod<?>> messages = new ArrayList<>();
        if (isMessageWithText(update)) {
            sessionManager.toDefaultUserSession(session);
            messages.add(sendClientNamesKeyboard(chatId));
        } else {
            handleSessionState(update, session, messages);
        }

        return messages;
    }

    private Long getChatId(Update update) {
        return update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
    }

    private boolean isMessageWithText(Update update) {
        return update.getMessage() != null && update.getMessage().hasText();
    }

    @SneakyThrows
    private void handleSessionState(Update update, UserSession session, List<BotApiMethod<?>> messages) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        ButtonCallback callback = objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class);

        LocalDate selectedDate = LocalDate.now();
        String callbackValue = callback.getValue();

        switch (session.getState()) {
            case AWAITING_NAME:
                session.setClientName(callbackValue);
                messages.addAll(
                        sendCalendarKeyBoard(
                                callbackQuery,
                                "Введите первую дату",
                                selectedDate
                        )
                );
                sessionManager.changeState(session);
                break;

            case AWAITING_FIRST_DATE:
                session.setFirstDate(parseDate(callbackValue));
                messages.addAll(
                        sendCalendarKeyBoard(
                                callbackQuery,
                                "Введите вторую дату",
                                selectedDate
                        )
                );
                sessionManager.changeState(session);
                break;

            case AWAITING_SECOND_DATE:
                session.setSecondDate(parseDate(callbackValue));
                messages.addAll(
                        sendClientSchedule(
                                callbackQuery.getMessage().getChatId(),
                                callbackQuery.getMessage().getMessageId(),
                                session
                        )
                );
                sessionManager.toDefaultUserSession(session);
                break;

            default:
                throw new IllegalStateException("Unknown session state: " + session.getState());
        }
    }

    private LocalDate parseDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateString, formatter);
    }

    @Override
    public String getCommand() {
        return "/clientschedule";
    }

    @SneakyThrows
    private List<BotApiMethod<?>> sendClientSchedule(long chatId, int messageId, UserSession userSession) {
        List<GetClientScheduleResponse> response = client.getClientSchedule(
                userSession.getClientName(),
                userSession.getFirstDate().atStartOfDay(),
                userSession.getSecondDate().atStartOfDay());

        EditMessageText editMessageText = editMessage(String.valueOf(chatId), messageId, formatClientSchedule(response), null);

        return List.of(editMessageText);
    }

    @SneakyThrows
    private SendMessage sendClientNamesKeyboard(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Выберите клиента для которого хотите получить расписание:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<ClientResponse> clients = client.getActiveClients();

        for (ClientResponse response : clients) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(response.getName());

            ButtonCallback callbackData = new ButtonCallback();
            callbackData.setCommand(getCommand());
            callbackData.setValue(response.getName());

            button.setCallbackData(objectMapper.writeValueAsString(callbackData));

            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(button);
            keyboard.add(rowInline);
        }

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        return sendMessage;
    }

    @SneakyThrows
    private List<BotApiMethod<?>> sendCalendarKeyBoard(CallbackQuery callbackQuery, String text, LocalDate date) {
        EditMessageText editMessageText = editMessage(
                String.valueOf(callbackQuery.getMessage().getChatId()),
                callbackQuery.getMessage().getMessageId(),
                text,
                createCalendarMarkup(date)
        );

        return List.of(editMessageText);
    }

    @SneakyThrows
    private InlineKeyboardMarkup createCalendarMarkup(LocalDate date) {
        YearMonth yearMonth = YearMonth.of(date.getYear(), date.getMonth());
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        LocalDate lastDayOfMonth = yearMonth.atEndOfMonth();

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<InlineKeyboardButton> headerRow = new ArrayList<>();

        ButtonCallback prevCallback = new ButtonCallback();
        prevCallback.setCommand(getCommand());
        prevCallback.setValue("<" + date.minusMonths(1));

        ButtonCallback nextCallback = new ButtonCallback();
        nextCallback.setCommand(getCommand());
        nextCallback.setValue(">" + date.plusMonths(1));

        headerRow.add(InlineKeyboardButton.builder()
                .text("◀")
                .callbackData(objectMapper.writeValueAsString(prevCallback))
                .build());
        headerRow.add(InlineKeyboardButton.builder()
                .text(date.getMonth().toString() + " " + date.getYear())
                .callbackData("ignore")
                .build());
        headerRow.add(InlineKeyboardButton.builder()
                .text("▶")
                .callbackData(objectMapper.writeValueAsString(prevCallback))
                .build());
        rowsInline.add(headerRow);

        List<InlineKeyboardButton> daysOfWeekRow = new ArrayList<>();
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            daysOfWeekRow.add(InlineKeyboardButton.builder()
                    .text(dayOfWeek.name().substring(0, 2))
                    .callbackData("ignore")
                    .build());
        }
        rowsInline.add(daysOfWeekRow);

        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        for (int i = 1; i < firstDayOfMonth.getDayOfWeek().getValue(); i++) {
            rowInline.add(InlineKeyboardButton.builder()
                    .text(" ")
                    .callbackData("ignore")
                    .build());
        }

        for (LocalDate day = firstDayOfMonth; !day.isAfter(lastDayOfMonth); day = day.plusDays(1)) {
            ButtonCallback dayCallback = new ButtonCallback();
            dayCallback.setCommand(getCommand());
            dayCallback.setValue(day.toString());

            rowInline.add(InlineKeyboardButton.builder()
                    .text(String.valueOf(day.getDayOfMonth()))
                    .callbackData(objectMapper.writeValueAsString(dayCallback))
                    .build());

            if (rowInline.size() == 7) {
                rowsInline.add(rowInline);
                rowInline = new ArrayList<>();
            }
        }

        if (!rowInline.isEmpty()) {
            while (rowInline.size() < 7) {
                rowInline.add(InlineKeyboardButton.builder()
                        .text(" ")
                        .callbackData("ignore")
                        .build());
            }
            rowsInline.add(rowInline);
        }

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    @SneakyThrows
    private List<BotApiMethod<?>> updateCalendar(Update update, String text) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data = callbackQuery.getData();

        ButtonCallback buttonCallback = objectMapper.readValue(data, ButtonCallback.class);
        LocalDate currentDate = LocalDate.parse(buttonCallback.getValue());

        EditMessageText editMessageText = editMessage(
                String.valueOf(callbackQuery.getMessage().getChatId()),
                callbackQuery.getMessage().getMessageId(),
                text,
                createCalendarMarkup(currentDate)
                );

        return List.of(editMessageText);
    }

    private EditMessageText editMessage(String chatId, int messageId, String text, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setReplyMarkup(keyboardMarkup);

        return editMessageText;
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
}