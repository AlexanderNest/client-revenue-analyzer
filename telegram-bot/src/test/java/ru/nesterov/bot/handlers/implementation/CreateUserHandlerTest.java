package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.dto.CreateUserRequest;
import ru.nesterov.dto.CreateUserResponse;
import ru.nesterov.dto.GetUserResponse;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        CreateUserHandler.class,
        ObjectMapper.class,
        BotHandlersRequestsKeeper.class
})
@SpringBootTest
public class CreateUserHandlerTest {
    @Autowired
    private CreateUserHandler createUserHandler;
    @Autowired
    private BotHandlersRequestsKeeper keeper;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;

    private String COMMAND;

    @BeforeEach
    public void setUp() {
        COMMAND = createUserHandler.getCommand();
    }

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

        CreateUserResponse createUserResponse = CreateUserResponse.builder()
                .userIdentifier(request.getUserIdentifier())
                .mainCalendarId(request.getMainCalendarId())
                .isCancelledCalendarEnabled(request.getIsCancelledCalendarEnabled())
                .cancelledCalendarId(request.getCancelledCalendarId())
                .build();

        when(client.createUser(any())).thenReturn(createUserResponse);

        Message message = new Message();
        message.setText(COMMAND);
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        when(client.getUserByUsername(any(GetUserRequest.class))).thenReturn(null);
        BotApiMethod<?> botApiMethod = createUserHandler.handle(update);

        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите ID основного календаря:", sendMessage.getText());

        message.setText(request.getMainCalendarId());
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Вы хотите сохранять информацию об отмененных мероприятиях с использованием второго календаря?", sendMessage.getText());

        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);

        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;

        List<List<InlineKeyboardButton>> keyboard = inlineKeyboardMarkup.getKeyboard();

        assertEquals(1, keyboard.size());

        String firstButtonText = keyboard.get(0).get(0).getText();
        String secondButtonText = keyboard.get(0).get(1).getText();
        assertEquals("Да", firstButtonText);
        assertEquals("Нет", secondButtonText);

        message.setText("Да");

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        ButtonCallback callback = new ButtonCallback();
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        callback.setCommand(COMMAND);
        callback.setValue(request.getIsCancelledCalendarEnabled().toString());
        String callbackData = callback.toShortString();
        callbackQuery.setData(callbackData);

        update.setCallbackQuery(callbackQuery);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;

        assertEquals("Введите ID календаря с отмененными мероприятиями:", sendMessage.getText());

        message.setText(request.getCancelledCalendarId());
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;

        assertEquals(String.join(System.lineSeparator(),
                        "Вы успешно зарегистрированы! ",
                        "ID пользователя: " + createUserResponse.getUserIdentifier(),
                        "ID основного календаря: " + createUserResponse.getMainCalendarId(),
                        "ID календаря с отмененными мероприятиями: " + createUserResponse.getCancelledCalendarId()),
                sendMessage.getText());
    }

    @Test
    void handleWithoutCancelledCalendar() throws JsonProcessingException {
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
        message.setText(COMMAND);
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        when(client.getUserByUsername(any(GetUserRequest.class))).thenReturn(null);
        BotApiMethod<?> botApiMethod = createUserHandler.handle(update);

        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите ID основного календаря:", sendMessage.getText());

        message.setText(request.getMainCalendarId());
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Вы хотите сохранять информацию об отмененных мероприятиях с использованием второго календаря?", sendMessage.getText());

        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);

        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;

        List<List<InlineKeyboardButton>> keyboard = inlineKeyboardMarkup.getKeyboard();

        String firstButtonText = keyboard.get(0).get(0).getText();
        String secondButtonText = keyboard.get(0).get(1).getText();
        assertEquals("Да", firstButtonText);
        assertEquals("Нет", secondButtonText);

        message.setText("Нет");

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(3L));
        ButtonCallback callback = new ButtonCallback();
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        callback.setCommand(COMMAND);
        callback.setValue(request.getIsCancelledCalendarEnabled().toString());
        callbackQuery.setData(objectMapper.writeValueAsString(callback));

        update.setCallbackQuery(callbackQuery);

        CreateUserResponse createUserResponse = CreateUserResponse.builder()
                .userIdentifier(request.getUserIdentifier())
                .mainCalendarId(request.getMainCalendarId())
                .isCancelledCalendarEnabled(request.getIsCancelledCalendarEnabled())
                .cancelledCalendarId(request.getCancelledCalendarId())
                .build();

        CreateUserRequest request1 = keeper.getRequest(user.getId(), CreateUserHandler.class, CreateUserRequest.class);

        when(client.createUser(request1)).thenReturn(createUserResponse);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;

        assertEquals(String.join(System.lineSeparator(),
                        "Вы успешно зарегистрированы! ",
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
        message.setText(COMMAND);
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

        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Вы уже зарегистрированы и можете пользоваться функциями бота", sendMessage.getText());
    }
}


