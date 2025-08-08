package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.nesterov.bot.config.BotProperties;
import ru.nesterov.bot.dto.GetUserRequest;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.implementation.invocable.GetMonthStatisticsCommandHandler;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.getSchedule.GetClientScheduleCommandHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        GetClientScheduleCommandHandler.class,
        GetMonthStatisticsCommandHandler.class,
        BotProperties.class
})
@EnableConfigurationProperties(BotProperties.class)
@TestPropertySource(properties = {
        "bot.menu-buttons-per-line=1"
})
public class UpdateUserControlButtonsHandlerTest extends RegisteredUserHandlerTest {
    @Test
    public void testForRegisteredUser() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(111L);
        User user = new User();
        user.setId(1L);
        message.setFrom(user);
        message.setChat(chat);
        message.setText("/start");
        update.setMessage(message);

        ReplyKeyboardMarkup replyKeyboardMarkupTest = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow1 = new KeyboardRow();
        keyboardRow1.add(new KeyboardButton("Узнать доход"));
        keyboardRows.add(keyboardRow1);

        KeyboardRow keyboardRow2 = new KeyboardRow();
        keyboardRow2.add(new KeyboardButton("Узнать расписание клиента"));
        keyboardRows.add(keyboardRow2);

        replyKeyboardMarkupTest.setKeyboard(keyboardRows);


        GetUserRequest getUserRequest = new GetUserRequest();
        getUserRequest.setUsername(user.getUserName());


        List<BotApiMethod<?>> result = updateUserControlButtonsHandler.handle(update);

        assertNotNull(result);
        assertEquals(SendMessage.class, result.get(0).getClass());

        SendMessage sendMessage = (SendMessage) result.get(0);
        assertEquals("111", sendMessage.getChatId());
        assertEquals("Выберите опцию:", sendMessage.getText());

        ReplyKeyboardMarkup replyKeyboardMarkup = (ReplyKeyboardMarkup) sendMessage.getReplyMarkup();
        assertNotNull(replyKeyboardMarkup);
        assertNotNull(replyKeyboardMarkup.getKeyboard());
        assertEquals(keyboardRows, replyKeyboardMarkup.getKeyboard());


        // проверяем, что количество кнопок на строке выставилось в соответствии с настройкой
        replyKeyboardMarkup.getKeyboard().stream()
                .map(ArrayList::size)
                .forEach(size -> assertEquals(1, size));
    }

    @Test
    public void testForUnregisteredUser(){
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(111L);
        User user = new User();
        user.setId(1L);
        message.setFrom(user);
        message.setChat(chat);
        message.setText("/start");
        update.setMessage(message);

        ReplyKeyboardMarkup replyKeyboardMarkupTest = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();

        keyboardRow.add(new KeyboardButton("Зарегистрироваться в боте"));
        keyboardRows.add(keyboardRow);
        replyKeyboardMarkupTest.setKeyboard(keyboardRows);

        when(client.getUserByUsername(any())).thenReturn(null);

        List<BotApiMethod<?>> result = updateUserControlButtonsHandler.handle(update);

        assertNotNull(result);
        assertEquals(SendMessage.class, result.get(0).getClass());

        SendMessage sendMessage = (SendMessage) result.get(0);
        assertEquals("111", sendMessage.getChatId());
        assertEquals("Выберите опцию:", sendMessage.getText());

        ReplyKeyboardMarkup replyKeyboardMarkup = (ReplyKeyboardMarkup) sendMessage.getReplyMarkup();
        assertNotNull(replyKeyboardMarkup);
        assertNotNull(replyKeyboardMarkup.getKeyboard());
        assertEquals(keyboardRows, replyKeyboardMarkup.getKeyboard());

        replyKeyboardMarkup.getKeyboard().stream()
                .map(ArrayList::size)
                .forEach(size -> assertEquals(1, size));
    }
}
