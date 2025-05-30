package ru.nesterov.bot.handlers.wrapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.implementation.GetClientScheduleCommandHandler;
import ru.nesterov.bot.handlers.implementation.GetMonthStatisticsCommandHandler;
import ru.nesterov.properties.BotProperties;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@EnableConfigurationProperties(BotProperties.class)
@TestPropertySource(properties = {
        "bot.buttons.update.interval=5"
})
public class UpdateUserControlButtonsHandlerWrapperTest extends RegisteredUserHandlerTest {

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

//        KeyboardRow keyboardRow2 = new KeyboardRow();
//        keyboardRow2.add(new KeyboardButton("Узнать расписание клиента"));
//        keyboardRows.add(keyboardRow2);

        replyKeyboardMarkupTest.setKeyboard(keyboardRows);

        when(updateUserControlButtonsHandler.getReplyKeyboardMarkup(update)).thenReturn(any()); // ????????????????????

        updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update);

            Thread.sleep(6000);


        ReplyKeyboardMarkup replyKeyboardMarkup = updateUserControlButtonsHandlerWrapper.getUpdateReplyKeyboardMarkup(update);

        assertNotNull(replyKeyboardMarkup);
        assertNotNull(replyKeyboardMarkup.getKeyboard());
        assertEquals(keyboardRows, replyKeyboardMarkup.getKeyboard());

    }
}
