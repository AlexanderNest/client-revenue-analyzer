package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetUserResponse;
import ru.nesterov.dto.GetUserRequest;

import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        CreateUserHandler.class
})
@SpringBootTest
public class CreateUserHandlerTest {
    @Autowired
    private CreateUserHandler createUserHandler;
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;

    @Test
    void handle() {
        Chat chat = new Chat();
        chat.setId(1L);

        User user = new User();
        user.setId(1L);

        CreateUserRequest request = CreateUserRequest.builder()
                .userIdentifier(user.getId().toString())
                .mainCalendarId("12345mc")
                .isCancelledCalendarEnabled(true)
                .cancelledCalendarId("12345cc")
                .build();

        Message message = new Message();
        message.setText("/register");
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        when(client.getUserByUsername(any(GetUserRequest.class))).thenReturn(null);
        BotApiMethod<?> botApiMethod = createUserHandler.handle(update);

        assertTrue(botApiMethod instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите ID основного календаря:", sendMessage.getText());

        message.setText(request.getMainCalendarId());
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Вы хотите сохранять информацию об отмененных мероприятиях с использованием второго календаря?", sendMessage.getText());

        message.setText(request.getIsCancelledCalendarEnabled().toString());
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите ID календаря с отмененными мероприятиями:", sendMessage.getText());

        CreateUserResponse createUserResponse = CreateUserResponse.builder()
                .userIdentifier(request.getUserIdentifier())
                .mainCalendarId(request.getMainCalendarId())
                .isCancelledCalendarEnabled(request.getIsCancelledCalendarEnabled())
                .cancelledCalendarId(request.getCancelledCalendarId())
                .build();

        when(client.createUser(createUserHandler.getCreateUserRequests().get(user.getId()))).thenReturn(createUserResponse);

        message.setText(request.getCancelledCalendarId());
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;

        assertEquals(String.join(System.lineSeparator(),
                        "Вы успешно зарегистрированы!",
                        " ",
                        "ID пользователя: " + createUserResponse.getUserIdentifier(),
                        "ID основного календаря: " + createUserResponse.getMainCalendarId(),
                        "ID календаря с отмененными мероприятиями: " + createUserResponse.getCancelledCalendarId()),
                sendMessage.getText());
    }

    @Test
    void handleWithoutCancelledCalendar() {
        Chat chat = new Chat();
        chat.setId(3L);

        User user = new User();
        user.setId(3L);

        CreateUserRequest request = CreateUserRequest.builder()
                .userIdentifier(user.getId().toString())
                .mainCalendarId("12345mc")
                .isCancelledCalendarEnabled(false)
                .build();

        Message message = new Message();
        message.setText("/register");
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        when(client.getUserByUsername(any(GetUserRequest.class))).thenReturn(null);
        BotApiMethod<?> botApiMethod = createUserHandler.handle(update);

        assertTrue(botApiMethod instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите ID основного календаря:", sendMessage.getText());

        message.setText(request.getMainCalendarId());
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Вы хотите сохранять информацию об отмененных мероприятиях с использованием второго календаря?", sendMessage.getText());

        message.setText(request.getIsCancelledCalendarEnabled().toString());
        update.setMessage(message);

        CreateUserResponse createUserResponse = CreateUserResponse.builder()
                .userIdentifier(request.getUserIdentifier())
                .mainCalendarId(request.getMainCalendarId())
                .isCancelledCalendarEnabled(request.getIsCancelledCalendarEnabled())
                .cancelledCalendarId(request.getCancelledCalendarId())
                .build();

        when(client.createUser(createUserHandler.getCreateUserRequests().get(user.getId()))).thenReturn(createUserResponse);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;

        assertEquals(String.join(System.lineSeparator(),
                        "Вы успешно зарегистрированы!",
                        " ",
                        "ID пользователя: " + createUserResponse.getUserIdentifier(),
                        "ID основного календаря: " + createUserResponse.getMainCalendarId(),
                        "ID календаря с отмененными мероприятиями: " + createUserResponse.getCancelledCalendarId()),
                sendMessage.getText());
    }

    @Test
    void handleCreatingTheSameUser() {
        Chat chat = new Chat();
        chat.setId(2L);

        User user = new User();
        user.setId(2L);

        Message message = new Message();
        message.setText("/register");
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        GetUserResponse response = GetUserResponse.builder()
                .userId(user.getId())
                .isCancelledCalendarEnabled(false)
                .mainCalendarId("main")
                .build();

        when(client.getUserByUsername(any(GetUserRequest.class))).thenReturn(response);

        BotApiMethod<?> botApiMethod = createUserHandler.handle(update);

        assertTrue(botApiMethod instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Вы уже зарегистрированы и можете пользоваться функциями бота", sendMessage.getText());
    }
}


