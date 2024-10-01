package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.ClientResponse;
import ru.nesterov.dto.GetClientScheduleResponse;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.bot.handlers.BotHandlersKeeper;
import ru.nesterov.dto.GetClientScheduleRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {
        GetClientScheduleHandler.class,
        ObjectMapper.class
})
public class GetClientScheduleHandlerTest {
    @Autowired
    private GetClientScheduleHandler handler;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;
    @MockBean
    private BotHandlersKeeper keeper;

    private static final String COMMAND = "/clientschedule";
    private static final String ENTER_FIRST_DATE = "Введите первую дату";
    private static final String ENTER_SECOND_DATE = "Введите вторую дату";

    @Test
    void handleCommandWhenMessageContainsText() {
        List<ClientResponse> clients = createActiveClients();
        when(client.getActiveClients(anyLong())).thenReturn(clients);

        Update update = createUpdateWithMessage();

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выберите клиента для которого хотите получить расписание:", sendMessage.getText());

        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);

        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;
        List<List<InlineKeyboardButton>> clientNamesKeyboard = inlineKeyboardMarkup.getKeyboard();

        assertEquals(4, clientNamesKeyboard.size());

        for (int i = 0; i < clientNamesKeyboard.size(); i++) {
            InlineKeyboardButton button = clientNamesKeyboard.get(i).get(0);
            assertEquals("Клиент " + (i + 1), button.getText());
        }
    }

    @SneakyThrows
    @Test
    void handleClientNameShouldReturnCalendarKeyboard() {
        setupUserRequest(
                null,
                LocalDate.of(2024, 9, 27),
                null,
                null);

        Update update = createUpdateWithCallbackQuery("Клиент 1");

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_FIRST_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        assertCalendar(calendarKeyboard, "SEPTEMBER 2024", 8);
    }

    @SneakyThrows
    @Test
    void handleFirstDateShouldReturnCalendarKeyboard() {
        setupUserRequest(
                null,
                LocalDate.of(2024, 9, 27),
                null,
                null);

        Update update = createUpdateWithCallbackQuery(String.valueOf(LocalDate.of(2024, 9, 27)));

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_SECOND_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        assertCalendar(calendarKeyboard, "SEPTEMBER 2024", 8);
    }

    @Test
    void handleSecondDateShouldReturnClientSchedule() {
        setupUserRequest(
                "Клиент 1",
                LocalDate.of(2024, 9, 27),
                LocalDate.of(2024, 9, 1),
                LocalDate.of(2024, 9, 30));

        List<GetClientScheduleResponse> clientSchedule = new ArrayList<>();

        GetClientScheduleResponse schedule1 = new GetClientScheduleResponse();
        schedule1.setEventStart(LocalDateTime.of(2024, 9, 2, 11, 30));
        schedule1.setEventEnd(LocalDateTime.of(2024, 9, 2, 12, 30));
        clientSchedule.add(schedule1);

        GetClientScheduleResponse schedule2 = new GetClientScheduleResponse();
        schedule2.setEventStart(LocalDateTime.of(2024, 9, 7, 11, 30));
        schedule2.setEventEnd(LocalDateTime.of(2024, 9, 7, 12, 30));
        clientSchedule.add(schedule2);

        GetClientScheduleResponse schedule3 = new GetClientScheduleResponse();
        schedule3.setEventStart(LocalDateTime.of(2024, 9, 30, 11, 30));
        schedule3.setEventEnd(LocalDateTime.of(2024, 9, 30, 12, 30));
        clientSchedule.add(schedule3);

        when(client.getClientSchedule(
                1L,
                "Клиент 1",
                LocalDate.of(2024, 9, 1).atStartOfDay(),
                LocalDate.of(2024, 9, 30).atStartOfDay()
        )).thenReturn(clientSchedule);

        Update update = createUpdateWithCallbackQuery(String.valueOf(LocalDate.of(2024, 9, 30)));
        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        String expectedText =
                "📅 Дата: 02 сентября 2024\n⏰ Время: 11:30 - 12:30\n\n" +
                        "📅 Дата: 07 сентября 2024\n⏰ Время: 11:30 - 12:30\n\n" +
                        "📅 Дата: 30 сентября 2024\n⏰ Время: 11:30 - 12:30";
        assertEquals(expectedText, editMessage.getText());
    }

    @Test
    void handleSwitchMonthWhenSelectedFirstDate1() {
        setupUserRequest(
                "Client 1",
                LocalDate.of(2024, 9, 27),
                null,
                null);

        Update update = createUpdateWithCallbackQuery("Prev");

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_FIRST_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        assertCalendar(calendarKeyboard, "AUGUST 2024", 7);
    }

    @Test
    void handleSwitchMonthWhenSelectedFirstDate2() {
        setupUserRequest(
                "Client 1",
                LocalDate.of(2024, 9, 27),
                null,
                null);

        Update update = createUpdateWithCallbackQuery("Next");

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_FIRST_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        assertCalendar(calendarKeyboard, "OCTOBER 2024", 7);
    }

    private void assertCalendar(List<List<InlineKeyboardButton>> calendarKeyboard, String displayedMonth, int expectedSize) {
        String[] parts = displayedMonth.split(" ");
        String monthName = parts[0];
        int year = Integer.parseInt(parts[1]);

        Month month = Month.valueOf(monthName);

        int daysInMonth = YearMonth.of(year, month.getValue()).lengthOfMonth();

        assertEquals(expectedSize, calendarKeyboard.size());

        List<InlineKeyboardButton> headerRow = calendarKeyboard.get(0);
        assertEquals(3, headerRow.size());
        assertEquals("◀", headerRow.get(0).getText());
        assertEquals(displayedMonth, headerRow.get(1).getText());
        assertEquals("▶", headerRow.get(2).getText());

        List<InlineKeyboardButton> daysOfWeekRow = calendarKeyboard.get(1);
        assertDaysOfWeek(daysOfWeekRow);

        for (int i = 2; i < calendarKeyboard.size(); i++) {
            for (InlineKeyboardButton dayButton : calendarKeyboard.get(i)) {
                if (!dayButton.getText().equals(" ")) {
                    int day = Integer.parseInt(dayButton.getText());
                    assertTrue(day >= 1 && day <= daysInMonth);
                }
            }
        }
    }

    private void setupUserRequest(
            String clientName,
            LocalDate displayedMonth,
            LocalDate firstDate,
            LocalDate secondDate) {
        Map<Long, Object> userRequest = new ConcurrentHashMap<>();
        userRequest.put(1L, GetClientScheduleRequest.builder()
                .userId(1L)
                .clientName(clientName)
                .displayedMonth(displayedMonth)
                .firstDate(firstDate)
                .secondDate(secondDate)
                .build());
        when(keeper.getHandlerKeeper(GetClientScheduleHandler.class)).thenReturn(userRequest);
    }

    private List<ClientResponse> createActiveClients() {
        ClientResponse client1 = new ClientResponse();
        client1.setId(1);
        client1.setName("Клиент 1");

        ClientResponse client2 = new ClientResponse();
        client2.setId(2);
        client2.setName("Клиент 2");

        ClientResponse client3 = new ClientResponse();
        client3.setId(3);
        client3.setName("Клиент 3");

        ClientResponse client4 = new ClientResponse();
        client4.setId(4);
        client4.setName("Клиент 4");

        return List.of(client1, client2, client3, client4);
    }

    private void assertDaysOfWeek(List<InlineKeyboardButton> daysOfWeekRow) {
        assertEquals(7, daysOfWeekRow.size());
        assertEquals("MO", daysOfWeekRow.get(0).getText());
        assertEquals("TU", daysOfWeekRow.get(1).getText());
        assertEquals("WE", daysOfWeekRow.get(2).getText());
        assertEquals("TH", daysOfWeekRow.get(3).getText());
        assertEquals("FR", daysOfWeekRow.get(4).getText());
        assertEquals("SA", daysOfWeekRow.get(5).getText());
        assertEquals("SU", daysOfWeekRow.get(6).getText());
    }

    private Update createUpdateWithMessage() {
        Chat chat = new Chat();
        chat.setId(1L);
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setText(COMMAND);
        message.setChat(chat);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }

    @SneakyThrows
    private Update createUpdateWithCallbackQuery(String callbackValue) {
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));

        User user = new User();
        user.setId(1L);
        callbackQuery.setFrom(user);

        Message message = new Message();
        message.setMessageId(1);
        Chat chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        callbackQuery.setMessage(message);

        ButtonCallback buttonCallback = new ButtonCallback();
        buttonCallback.setCommand(COMMAND);
        buttonCallback.setValue(callbackValue);
        callbackQuery.setData(objectMapper.writeValueAsString(buttonCallback));

        Update update = new Update();
        update.setCallbackQuery(callbackQuery);

        return update;
    }
}