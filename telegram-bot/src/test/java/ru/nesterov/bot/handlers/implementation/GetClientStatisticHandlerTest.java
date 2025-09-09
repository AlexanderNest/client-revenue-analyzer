package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.getClientStatisticHandler.GetClientStatisticHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        GetClientStatisticHandler.class
})
public class GetClientStatisticHandlerTest extends RegisteredUserHandlerTest {
    @Autowired
    private GetClientStatisticHandler handler;

    private static final String COMMAND = "Узнать статистику по клиенту";

    @AfterEach
    public void resetHandler() {
        handler.resetState(1);
    }

    @Test
    void handleCommandWhenMessageContainsText() {
        List<GetActiveClientResponse> clients = createActiveClients();
        when(client.getActiveClients(anyLong())).thenReturn(clients);

        Update update = createUpdateWithCommand();

        List<BotApiMethod<?>> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod.get(0));

        SendMessage sendMessage = (SendMessage) botApiMethod.get(0);
        assertEquals("Выберите клиента, чью статистику хотите узнать:", sendMessage.getText());

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

    @Test
    void handleCommandWhenNoClientsFound() {
        Update update = createUpdateWithCommand();

        List<BotApiMethod<?>> botApiMethod = handler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod.get(0));
        SendMessage sendMessage = (SendMessage) botApiMethod.get(0);

        assertEquals("Нет доступных клиентов", sendMessage.getText());
    }

    @Test
    void sendClientNamesKeyboardShouldReturnSortedClients() {

        GetActiveClientResponse client1 = new GetActiveClientResponse();
        client1.setName("алексей");

        GetActiveClientResponse client2 = new GetActiveClientResponse();
        client2.setName("Яна");

        GetActiveClientResponse client3 = new GetActiveClientResponse();
        client3.setName("Андрей");

        GetActiveClientResponse client4 = new GetActiveClientResponse();
        client4.setName("борис");

        GetActiveClientResponse client5 = new GetActiveClientResponse();
        client5.setName("Борис");

        List<GetActiveClientResponse> unsortedClients = new ArrayList<>(List.of(client1, client2, client3, client4, client5));

        when(client.getActiveClients(anyLong())).thenReturn(unsortedClients);

        Update update = createUpdateWithCommand();
        List<BotApiMethod<?>> result = handler.handle(update);

        assertInstanceOf(SendMessage.class, result.get(0));
        SendMessage sendMessage = (SendMessage) result.get(0);

        InlineKeyboardMarkup markup = (InlineKeyboardMarkup) sendMessage.getReplyMarkup();
        List<List<InlineKeyboardButton>> keyboard = markup.getKeyboard();

        assertEquals(5, keyboard.size());

        assertEquals("алексей", keyboard.get(0).get(0).getText());
        assertEquals("Андрей", keyboard.get(1).get(0).getText());
        assertEquals("борис", keyboard.get(2).get(0).getText());
        assertEquals("Борис", keyboard.get(3).get(0).getText());
        assertEquals("Яна", keyboard.get(4).get(0).getText());
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

    private Update createUpdateWithCommand() {
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
}
