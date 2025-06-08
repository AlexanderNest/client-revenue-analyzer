package ru.nesterov.bot.handlers.wrapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@EnableConfigurationProperties(BotProperties.class)
@TestPropertySource(properties = {
        "bot.buttons.update.interval=10"
})
public class UpdateUserControlButtonsHandlerWrapperTest extends RegisteredUserHandlerTest {
    @Autowired
    private UpdateUserControlButtonsHandlerWrapper updateUserControlButtonsHandlerWrapper;
    @MockBean
    private UpdateUserControlButtonsHandler updateUserControlButtonsHandler;

    @Test
    public void getUpdateReplyKeyboardMarkupTest() throws InterruptedException {
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

        assertNull(updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update));
        when(updateUserControlButtonsHandler.getReplyKeyboardMarkup(update)).thenReturn(replyKeyboardMarkupTest);
        assertNull(updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update));

        Thread.sleep(10);

        assertEquals(1, updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update).getKeyboard().size());
    }
}
