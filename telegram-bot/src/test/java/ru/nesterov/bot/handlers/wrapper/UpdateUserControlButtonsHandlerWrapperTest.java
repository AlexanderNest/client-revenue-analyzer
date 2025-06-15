package ru.nesterov.bot.handlers.wrapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.nesterov.bot.config.BotProperties;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.implementation.invocable.UpdateUserControlButtonsHandler;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@EnableConfigurationProperties(BotProperties.class)
@TestPropertySource(properties = {
        "bot.buttons.update.interval=10"
})
public class UpdateUserControlButtonsHandlerWrapperTest extends RegisteredUserHandlerTest {
    @SpyBean
    private UpdateUserControlButtonsHandlerWrapper updateUserControlButtonsHandlerWrapper;
    @MockBean
    private UpdateUserControlButtonsHandler updateUserControlButtonsHandler;

    @Test
    public void getUpdateReplyKeyboardMarkupTest() {
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
        keyboardRow1.add(new KeyboardButton("Зарегистрироваться в боте"));
        keyboardRows.add(keyboardRow1);

        replyKeyboardMarkupTest.setKeyboard(keyboardRows);

        updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update);

        Mockito.when(updateUserControlButtonsHandlerWrapper.getTimeInterval(111L))
                .thenCallRealMethod() // 0 вызов - вызов реального метода
                .thenReturn(10L)   // 1-й вызов → 1000 мс
                .thenReturn(10000L);    // 2-й вызов → 5000 м

        when(updateUserControlButtonsHandler.getReplyKeyboardMarkup(update)).thenReturn(replyKeyboardMarkupTest);

        ReplyKeyboardMarkup replyKeyboardMarkup1 = updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update);
        assertNull(replyKeyboardMarkup1);

        ReplyKeyboardMarkup replyKeyboardMarkup2 = updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update);
        assertNotEquals(replyKeyboardMarkup2, replyKeyboardMarkupTest);

        ReplyKeyboardMarkup replyKeyboardMarkup3 = updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update);
        assertEquals(replyKeyboardMarkup3, replyKeyboardMarkupTest);
    }
}
