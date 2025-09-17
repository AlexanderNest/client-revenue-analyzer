package ru.nesterov.bot.handlers.implementation;

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
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.dto.GetClientStatisticResponse;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.getClientStatisticHandler.GetClientStatisticHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        GetClientStatisticHandler.class
})
class GetClientStatisticHandlerTest2 extends RegisteredUserHandlerTest {

    @Autowired
    private GetClientStatisticHandler handler;

    private static final String COMMAND = "Узнать статистику по клиенту";

    @Test
    void shouldShowNoClientsMessage() {
        // given
        Update update = createUpdateWithMessage("Узнать статистику по клиенту");
        when(client.getActiveClients(anyLong())).thenReturn(List.of());

        // when
        List<BotApiMethod<?>> result = handler.handle(update);

        // then
        assertEquals(1, result.size());
        SendMessage message = (SendMessage) result.get(0);
        assertEquals("Нет доступных клиентов", message.getText());
    }

    @Test
    void shouldShowClientsKeyboard() {
        // given
        Update update = createUpdateWithMessage("Узнать статистику по клиенту");
        List<GetActiveClientResponse> clients = createActiveClients();
        when(client.getActiveClients(anyLong())).thenReturn(clients);

        // when
        List<BotApiMethod<?>> result = handler.handle(update);

        // then
        assertEquals(1, result.size());
        SendMessage message = (SendMessage) result.get(0);
        assertEquals("Выберите клиента, чью статистику хотите узнать:", message.getText());
    }

    @Test
    void shouldHandleClientSelectionWithNoStatistics() {
        // given
        Update update = createCallbackUpdate("Client A");
        when(client.getClientStatistic(anyLong(), eq("Client A"))).thenReturn(null);

        // when
        List<BotApiMethod<?>> result = handler.handle(update);

        // then
        assertEquals(1, result.size());
        SendMessage message = (SendMessage) result.get(0);
        assertEquals("У пользователя нет встреч", message.getText());
    }

    @Test
    void shouldHandleClientSelectionWithStatistics() {
        // given
        Update update = createCallbackUpdate("Client A");
        GetClientStatisticResponse response = new GetClientStatisticResponse();
        response.setName("Client A");
        response.setId(123L);
        response.setPhone("+123456789");
        response.setDescription("Test Client");
        response.setStartDate(new Date());
        response.setServiceDuration(30);
        response.setSuccessfulMeetingsHours(10);
        response.setCancelledMeetingsHours(2);
        response.setIncomePerHour(500);
        response.setSuccessfulEventsCount(10);
        response.setPlannedCancelledEventsCount(1);
        response.setNotPlannedCancelledEventsCount(1);
        response.setTotalIncome(5000);

        when(client.getClientStatistic(anyLong(), eq("Client A"))).thenReturn(response);

        // when
        List<BotApiMethod<?>> result = handler.handle(update);

        // then
        assertEquals(1, result.size());
        assertInstanceOf(EditMessageText.class, result.get(0));
    }

    private Update createCallbackUpdate(String callbackValue) {
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
        callbackQuery.setData(buttonCallbackService.getTelegramButtonCallbackString(buttonCallback));

        Update update = new Update();
        update.setCallbackQuery(callbackQuery);

        return update;
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

        return new ArrayList<>(List.of(client1, client2, client3, client4));
    }
}
