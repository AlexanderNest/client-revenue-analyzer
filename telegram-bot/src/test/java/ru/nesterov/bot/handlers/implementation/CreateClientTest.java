package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.nesterov.bot.dto.CreateClientRequest;
import ru.nesterov.bot.dto.CreateClientResponse;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.callback.ButtonCallback;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.createClient.CreateClientHandler;
import ru.nesterov.bot.handlers.service.ButtonCallbackService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        CreateClientHandler.class,
})
public class CreateClientTest extends RegisteredUserHandlerTest {
    @Autowired
    private CreateClientHandler createClientHandler;
    @Autowired
    private ButtonCallbackService buttonCallbackService;
    private String COMMAND;

    @BeforeEach
    public void setUp() {
        COMMAND = createClientHandler.getCommand();
    }

    @Test
    void handle() {
        Chat chat = new Chat();
        chat.setId(1L);

        User user = new User();
        user.setId(1L);

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        CreateClientRequest request = CreateClientRequest.builder()
                .name("Masha")
                .pricePerHour(2000)
                .phone("8916")
                .idGenerationNeeded(false)
                .description("description")
                .build();

        CreateClientResponse response = CreateClientResponse.builder()
                .name("Masha")
                .pricePerHour(2000)
                .active(true)
                .phone("8916")
                .startDate(new Date())
                .description("description")
                .build();

        when(client.createClient(any(), any())).thenReturn(response);

        Message message = new Message();
        message.setText(COMMAND);
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        BotApiMethod<?> botApiMethod = createClientHandler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите имя", sendMessage.getText());

        message.setText(request.getName());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите стоимость за час", sendMessage.getText());

        message.setText(request.getPricePerHour().toString());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите описание", sendMessage.getText());

        message.setText(request.getDescription());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите номер телефона", sendMessage.getText());

        message.setText(request.getPhone());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Включить генерацию нового имени, если клиент с таким именем уже существует?", sendMessage.getText());

        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);

        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;

        List<List<InlineKeyboardButton>> keyboard = inlineKeyboardMarkup.getKeyboard();

        assertEquals(1, keyboard.size());

        String firstButtonText = keyboard.get(0).get(0).getText();
        String secondButtonText = keyboard.get(0).get(1).getText();
        assertEquals("Да", firstButtonText);
        assertEquals("Нет", secondButtonText);

        message.setText(null);
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        ButtonCallback callback = new ButtonCallback();
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        callback.setCommand(COMMAND);
        callback.setValue(request.getIdGenerationNeeded().toString());
        String callbackData = buttonCallbackService.getTelegramButtonCallbackString(callback);
        callbackQuery.setData(callbackData);

        update.setCallbackQuery(callbackQuery);

        botApiMethod = createClientHandler.handle(update); //TODO тут почему-то в message возвращается Нет. Хотя должен прийти только в колбеке (по крайней мере так кажется). Надо проверить, как в реальности приходит это значение. Если оно правда там есть, то написать мне. Если его там нет, исправить тест
        sendMessage = (SendMessage) botApiMethod;
        assertEquals(String.join(System.lineSeparator(),
                "Клиент успешно зарегистрирован!",
                "Имя: Masha",
                "Стоимость за час: 2000",
                "Описание: description",
                "Дата начала встреч: " + formatter.format(response.getStartDate()),
                "Номер телефона: 8916"),
        sendMessage.getText());
    }

    @Test
    void handleCreateClientWithTheSameNameWithoutIdGeneration() {
        Chat chat = new Chat();
        chat.setId(2L);

        User user = new User();
        user.setId(2L);

        CreateClientRequest request = CreateClientRequest.builder()
                .name("Dasha")
                .pricePerHour(2000)
                .phone("8916")
                .idGenerationNeeded(false)
                .description("description")
                .build();

        CreateClientResponse response = CreateClientResponse.builder()
                .responseCode(409)
                .build();

        when(client.createClient(any(), any())).thenReturn(response);

        Message message = new Message();
        message.setText(COMMAND);
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        BotApiMethod<?> botApiMethod = createClientHandler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите имя", sendMessage.getText());

        message.setText(request.getName());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите стоимость за час", sendMessage.getText());

        message.setText(request.getPricePerHour().toString());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите описание", sendMessage.getText());

        message.setText(request.getDescription());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите номер телефона", sendMessage.getText());

        message.setText(request.getPhone());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Включить генерацию нового имени, если клиент с таким именем уже существует?", sendMessage.getText());

        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);

        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;

        List<List<InlineKeyboardButton>> keyboard = inlineKeyboardMarkup.getKeyboard();

        assertEquals(1, keyboard.size());

        String firstButtonText = keyboard.get(0).get(0).getText();
        String secondButtonText = keyboard.get(0).get(1).getText();
        assertEquals("Да", firstButtonText);
        assertEquals("Нет", secondButtonText);

        message.setText(null);

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        ButtonCallback callback = new ButtonCallback();
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        callback.setCommand(COMMAND);
        callback.setValue(request.getIdGenerationNeeded().toString());
        String callbackData = buttonCallbackService.getTelegramButtonCallbackString(callback);
        callbackQuery.setData(callbackData);

        update.setCallbackQuery(callbackQuery);

        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;

        assertEquals("Клиент с таким именем уже создан", sendMessage.getText());
    }

    @Test
    void handleCreateClientWithTheSameNameWithIdGeneration() {
        Chat chat = new Chat();
        chat.setId(3L);

        User user = new User();
        user.setId(3L);

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        CreateClientRequest request = CreateClientRequest.builder()
                .name("Sasha")
                .pricePerHour(2000)
                .phone("8916")
                .idGenerationNeeded(false)
                .description("description")
                .build();

        CreateClientResponse response = CreateClientResponse.builder()
                .name("Sasha 2")
                .pricePerHour(2000)
                .active(true)
                .phone("8916")
                .startDate(new Date())
                .description("description")
                .build();

        when(client.createClient(any(), any())).thenReturn(response);

        Message message = new Message();
        message.setText(COMMAND);
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        BotApiMethod<?> botApiMethod = createClientHandler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);
        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите имя", sendMessage.getText());

        message.setText(request.getName());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите стоимость за час", sendMessage.getText());

        message.setText(request.getPricePerHour().toString());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите описание", sendMessage.getText());

        message.setText(request.getDescription());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Введите номер телефона", sendMessage.getText());

        message.setText(request.getPhone());
        update.setMessage(message);
        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Включить генерацию нового имени, если клиент с таким именем уже существует?", sendMessage.getText());

        ReplyKeyboard markup = sendMessage.getReplyMarkup();
        assertInstanceOf(InlineKeyboardMarkup.class, markup);

        InlineKeyboardMarkup inlineKeyboardMarkup = (InlineKeyboardMarkup) markup;

        List<List<InlineKeyboardButton>> keyboard = inlineKeyboardMarkup.getKeyboard();

        assertEquals(1, keyboard.size());

        String firstButtonText = keyboard.get(0).get(0).getText();
        String secondButtonText = keyboard.get(0).get(1).getText();
        assertEquals("Да", firstButtonText);
        assertEquals("Нет", secondButtonText);

        message.setText(null);

        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setId(String.valueOf(1));
        ButtonCallback callback = new ButtonCallback();
        callbackQuery.setFrom(user);
        callbackQuery.setMessage(message);
        callback.setCommand(COMMAND);
        callback.setValue(request.getIdGenerationNeeded().toString());
        String callbackData = buttonCallbackService.getTelegramButtonCallbackString(callback);
        callbackQuery.setData(callbackData);

        update.setCallbackQuery(callbackQuery);

        botApiMethod = createClientHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals(String.join(System.lineSeparator(),
                        "Клиент успешно зарегистрирован!",
                        "Имя: Sasha 2",
                        "Стоимость за час: 2000",
                        "Описание: description",
                        "Дата начала встреч: " + formatter.format(response.getStartDate()),
                        "Номер телефона: 8916"),
                sendMessage.getText());
    }
}
