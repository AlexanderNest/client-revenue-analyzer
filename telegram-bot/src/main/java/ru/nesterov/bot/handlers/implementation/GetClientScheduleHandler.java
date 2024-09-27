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
import ru.nesterov.InlineCalendarBuilder;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.ClientResponse;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.session.BotSessionManager;
import ru.nesterov.session.UserData;
import ru.nesterov.session.UserDataCheck;

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
    private final BotSessionManager sessionManager;

    public GetClientScheduleHandler(ObjectMapper objectMapper, ClientRevenueAnalyzerIntegrationClient client, BotSessionManager sessionManager) {
        super(objectMapper, client);
        this.sessionManager = sessionManager;

//        sessionManager.getData(GetClientScheduleHandlerClientRevenue.class, 12L, UserData.class);
    }

    @SneakyThrows
    @Override
    public List<BotApiMethod<?>> handle(Update update) {
        Long chatId = getChatId(update);
        Long userId = getUserId(update);
        UserData userData = sessionManager.getUserData(chatId, userId);

        List<BotApiMethod<?>> messages = new ArrayList<>();
        if (isMessageWithText(update)) {
            sessionManager.setDefaultUserData(userData);
            messages.add(sendClientNamesKeyboard(chatId, userId));
        } else {
            handleCallbackQuery(update, userData, messages);
        }
        return messages;
    }

    @SneakyThrows
    private void handleCallbackQuery(Update update, UserData userData, List<BotApiMethod<?>> messages) throws Exception {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String callbackData = callbackQuery.getData();
        ButtonCallback callback = objectMapper.readValue(callbackData, ButtonCallback.class);

        if (isValidDate(callback.getValue())) {
            handleSelectedDate(callbackQuery, userData, messages);
        } else {
            switch (callback.getValue()) {
                case "Next":
                case "Prev":
                    handleMonthSwitch(callbackQuery, userData, messages);
                    break;
                default:
                    handleClientName(callbackQuery.getMessage().getChatId(), callbackQuery.getMessage().getMessageId(), userData, callback.getValue(), messages);
                    break;
            }
        }
    }

    @Override
    public String getCommand() {
        return "/clientschedule";
    }

    @SneakyThrows
    private List<BotApiMethod<?>> sendClientSchedule(long chatId, int messageId, UserData userData) {
        List<GetClientScheduleResponse> response = client.getClientSchedule(
                userData.getUserId(),
                userData.getClientName(),
                userData.getFirstDate().atStartOfDay(),
                userData.getSecondDate().atStartOfDay());

        EditMessageText editMessageText = editMessage(String.valueOf(chatId), messageId, formatClientSchedule(response), null);

        return List.of(editMessageText);
    }

    @SneakyThrows
    private SendMessage sendClientNamesKeyboard(long chatId, long userId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText("Выберите клиента для которого хотите получить расписание:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<ClientResponse> clients = client.getActiveClients(userId);

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
    private List<BotApiMethod<?>> sendCalendarKeyBoard(long chatId, int messageId, String text, LocalDate date) {
        EditMessageText editMessageText = editMessage(
                String.valueOf(chatId),
                messageId,
                text,
                InlineCalendarBuilder.createCalendarMarkup(date, objectMapper, getCommand())
        );

        return List.of(editMessageText);
    }

    private void handleClientName(long chatId, int messageId, UserData userData, String clientName, List<BotApiMethod<?>> messages) {
        if (userData.getClientName() == null) {
            userData.setClientName(clientName);
            messages.addAll(sendCalendarKeyBoard(chatId, messageId, "Введите первую дату", userData.getCurrentDate()));
        }
    }

    @SneakyThrows
    private void handleMonthSwitch(CallbackQuery callbackQuery, UserData userData, List<BotApiMethod<?>> messages) {
        ButtonCallback callback = objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class);
        if (callback.getValue().equals("Next")) {
            userData.setCurrentDate(userData.getCurrentDate().plusMonths(1));
        } else if (callback.getValue().equals("Prev")) {
            userData.setCurrentDate(userData.getCurrentDate().minusMonths(1));
        }

        String calendarMessage = "";
        if (UserDataCheck.FIRST_DATE_MISSING.validate(userData)) {
            calendarMessage = "Введите первую дату";
        } else if (UserDataCheck.SECOND_DATE_MISSING.validate(userData)) {
            calendarMessage = "Введите вторую дату";
        }

        messages.addAll(sendCalendarKeyBoard(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getMessage().getMessageId(),
                calendarMessage,
                userData.getCurrentDate()
        ));
    }

    @SneakyThrows
    private void handleSelectedDate(CallbackQuery callbackQuery, UserData userData, List<BotApiMethod<?>> messages) {
        ButtonCallback callback = objectMapper.readValue(callbackQuery.getData(), ButtonCallback.class);
        if (UserDataCheck.FIRST_DATE_MISSING.validate(userData)) {
            userData.setFirstDate(LocalDate.parse(callback.getValue()));
            messages.addAll(sendCalendarKeyBoard(
                    callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId(),
                    UserDataCheck.SECOND_DATE_MISSING.getMessage(),
                    userData.getCurrentDate()
            ));
        } else {
            userData.setSecondDate(LocalDate.parse(callback.getValue()));
            messages.addAll(sendClientSchedule(
                    callbackQuery.getMessage().getChatId(),
                    callbackQuery.getMessage().getMessageId(),
                    userData));
        }
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