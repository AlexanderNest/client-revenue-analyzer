package ru.nesterov.bot.handlers.implementation;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.nesterov.bot.handlers.RegisteredUserHandler;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.GetActiveClientResponse;
import ru.nesterov.dto.GetClientScheduleResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        GetClientScheduleHandler.class
})
public class GetClientScheduleHandlerTest extends RegisteredUserHandler {
    @Autowired
    private GetClientScheduleHandler handler;

    private static final String COMMAND = "Узнать расписание клиента";
    private static final String ENTER_FIRST_DATE = "Введите первую дату";
    private static final String ENTER_SECOND_DATE = "Введите вторую дату";

    @Test
    void handleCommandWhenMessageContainsText() {
        List<GetActiveClientResponse> clients = createActiveClients();
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
        Update updateWithCommand = createUpdateWithMessage();
        handler.handle(updateWithCommand);
        Update updateWithClientName = createUpdateWithCallbackQuery("Клиент 1");

        BotApiMethod<?> botApiMethod = handler.handle(updateWithClientName);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_FIRST_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        LocalDate expectedDisplayedDate = LocalDate.now();
        assertCalendar(calendarKeyboard, expectedDisplayedDate);
    }

    @SneakyThrows
    @Test
    void handleFirstDateShouldReturnCalendarKeyboard() {
        Update updateWithCommand = createUpdateWithMessage();
        handler.handle(updateWithCommand);
        Update updateWithClientName = createUpdateWithCallbackQuery("Клиент 1");
        handler.handle(updateWithClientName);
        LocalDate firstDate = LocalDate.now();
        Update updateWithFirstDate = createUpdateWithCallbackQuery(String.valueOf(firstDate));

        BotApiMethod<?> botApiMethod = handler.handle(updateWithFirstDate);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_SECOND_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        assertCalendar(calendarKeyboard, firstDate);
    }

    @Test
    void handleSecondDateShouldReturnClientSchedule() {
        Update updateWithCommand = createUpdateWithMessage();
        handler.handle(updateWithCommand);
        Update updateWithClientName = createUpdateWithCallbackQuery("Клиент 1");
        handler.handle(updateWithClientName);
        LocalDate firstDate = LocalDate.now();
        Update updateWithFirstDate = createUpdateWithCallbackQuery(String.valueOf(firstDate));
        handler.handle(updateWithFirstDate);

        List<GetClientScheduleResponse> clientSchedule = new ArrayList<>();

        GetClientScheduleResponse schedule1 = new GetClientScheduleResponse();
        LocalDateTime eventStart1 = LocalDateTime.now();
        schedule1.setEventStart(eventStart1);
        schedule1.setEventEnd(eventStart1.plusHours(1L));
        clientSchedule.add(schedule1);

        GetClientScheduleResponse schedule2 = new GetClientScheduleResponse();
        LocalDateTime eventStart2 = LocalDateTime.now().plusDays(1L);
        schedule2.setEventStart(eventStart2);
        schedule2.setEventEnd(eventStart2.plusHours(1L));
        clientSchedule.add(schedule2);

        GetClientScheduleResponse schedule3 = new GetClientScheduleResponse();
        LocalDateTime eventStart3 = LocalDateTime.now().plusDays(2L);
        schedule3.setEventStart(eventStart3);
        schedule3.setEventEnd(eventStart3.plusHours(1L));
        clientSchedule.add(schedule3);

        LocalDate secondDate = firstDate.plusMonths(1L);

        when(client.getClientSchedule(
                1L,
                "Клиент 1",
                firstDate.atStartOfDay(),
                secondDate.atStartOfDay()
        )).thenReturn(clientSchedule);

        Update updateWithSecondDate = createUpdateWithCallbackQuery(String.valueOf(secondDate));
        BotApiMethod<?> botApiMethod = handler.handle(updateWithSecondDate);
        assertInstanceOf(EditMessageText.class, botApiMethod);
        EditMessageText editMessage = (EditMessageText) botApiMethod;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("ru"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.forLanguageTag("ru"));

        String expectedText = String.join("\n\n",
                String.format(
                        "📅 Дата: %s\n⏰ Время: %s - %s",
                        LocalDateTime.now().format(dateFormatter),
                        LocalDateTime.now().format(timeFormatter),
                        LocalDateTime.now().plusHours(1).format(timeFormatter)
                ),
                String.format(
                        "📅 Дата: %s\n⏰ Время: %s - %s",
                        LocalDateTime.now().plusDays(1).format(dateFormatter),
                        LocalDateTime.now().plusDays(1).format(timeFormatter),
                        LocalDateTime.now().plusDays(1).plusHours(1).format(timeFormatter)
                ),
                String.format(
                        "📅 Дата: %s\n⏰ Время: %s - %s",
                        LocalDateTime.now().plusDays(2).format(dateFormatter),
                        LocalDateTime.now().plusDays(2).format(timeFormatter),
                        LocalDateTime.now().plusDays(2).plusHours(1).format(timeFormatter)
                )
        );
        assertEquals(expectedText, editMessage.getText());
    }

    @Test
    void handleSwitchMonthWhenSelectedFirstDate1() {
        Update updateWithCommand = createUpdateWithMessage();
        handler.handle(updateWithCommand);
        Update updateWithClientName = createUpdateWithCallbackQuery("Клиент 1");
        handler.handle(updateWithClientName);

        Update update = createUpdateWithCallbackQuery("Prev");

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_FIRST_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        LocalDate expectedDisplayedDate = LocalDate.now().minusMonths(1L);
        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        assertCalendar(calendarKeyboard, expectedDisplayedDate);
    }

    @Test
    void handleSwitchMonthWhenSelectedFirstDate2() {
        Update updateWithCommand = createUpdateWithMessage();
        handler.handle(updateWithCommand);
        Update updateWithClientName = createUpdateWithCallbackQuery("Клиент 1");
        handler.handle(updateWithClientName);

        Update update = createUpdateWithCallbackQuery("Next");

        BotApiMethod<?> botApiMethod = handler.handle(update);
        assertInstanceOf(EditMessageText.class, botApiMethod);

        EditMessageText editMessage = (EditMessageText) botApiMethod;
        assertEquals(ENTER_FIRST_DATE, editMessage.getText());

        InlineKeyboardMarkup markup = editMessage.getReplyMarkup();
        assertNotNull(markup);
        assertFalse(markup.getKeyboard().isEmpty());

        LocalDate expectedDisplayedDate = LocalDate.now().plusMonths(1L);
        List<List<InlineKeyboardButton>> calendarKeyboard = markup.getKeyboard();
        assertCalendar(calendarKeyboard, expectedDisplayedDate);
    }

    private void assertCalendar(List<List<InlineKeyboardButton>> calendarKeyboard, LocalDate displayedDate) {
        String month = displayedDate.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, new Locale("ru")).toUpperCase();
        String year = String.valueOf(displayedDate.getYear());

        int daysInMonth = displayedDate.lengthOfMonth();

        List<InlineKeyboardButton> headerRow = calendarKeyboard.get(0);
        assertEquals(3, headerRow.size());
        assertEquals("◀", headerRow.get(0).getText());
        assertEquals(month + " " + year, headerRow.get(1).getText());
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

    private List<GetActiveClientResponse> createActiveClients() {
        GetActiveClientResponse client1 = new GetActiveClientResponse();
        client1.setId(1);
        client1.setName("Клиент 1");

        GetActiveClientResponse client2 = new GetActiveClientResponse();
        client2.setId(2);
        client2.setName("Клиент 2");

        GetActiveClientResponse client3 = new GetActiveClientResponse();
        client3.setId(3);
        client3.setName("Клиент 3");

        GetActiveClientResponse client4 = new GetActiveClientResponse();
        client4.setId(4);
        client4.setName("Клиент 4");

        return List.of(client1, client2, client3, client4);
    }

    private void assertDaysOfWeek(List<InlineKeyboardButton> daysOfWeekRow) {
        assertEquals(7, daysOfWeekRow.size());
        assertEquals("ПН", daysOfWeekRow.get(0).getText());
        assertEquals("ВТ", daysOfWeekRow.get(1).getText());
        assertEquals("СР", daysOfWeekRow.get(2).getText());
        assertEquals("ЧТ", daysOfWeekRow.get(3).getText());
        assertEquals("ПТ", daysOfWeekRow.get(4).getText());
        assertEquals("СБ", daysOfWeekRow.get(5).getText());
        assertEquals("ВС", daysOfWeekRow.get(6).getText());
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
