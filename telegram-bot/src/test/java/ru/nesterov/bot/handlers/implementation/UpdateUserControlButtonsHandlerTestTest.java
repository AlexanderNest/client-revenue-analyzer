package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.nesterov.bot.TelegramUpdateUtils;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.properties.BotProperties;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@ContextConfiguration(classes = {
        GetClientScheduleCommandHandler.class,
        GetMonthStatisticsCommandHandler.class,
        UpdateUserControlButtonsHandler.class,
        BotProperties.class
})
@EnableConfigurationProperties(BotProperties.class)
@TestPropertySource(properties = {
        "bot.menu-buttons-per-line=1"
})
public class UpdateUserControlButtonsHandlerTestTest extends RegisteredUserHandlerTest {
    @Autowired
    private UpdateUserControlButtonsHandler updateUserControlButtonsHandler;
    @Autowired
    private BotProperties botProperties;

    @Test
    public void test() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(111L);
        message.setChat(chat);
        message.setText("/start");
        update.setMessage(message);

        BotApiMethod<?> result = updateUserControlButtonsHandler.handle(update);

        assertNotNull(result);
        assertEquals(SendMessage.class, result.getClass());

        SendMessage sendMessage = (SendMessage) result;
        assertEquals("111", sendMessage.getChatId());
        assertEquals("Выберите опцию:", sendMessage.getText());

        ReplyKeyboardMarkup replyKeyboardMarkup = (ReplyKeyboardMarkup) sendMessage.getReplyMarkup();
        assertNotNull(replyKeyboardMarkup);
        assertNotNull(replyKeyboardMarkup.getKeyboard());

        replyKeyboardMarkup.getKeyboard().stream()
                .map(ArrayList::size)
                .forEach(size -> assertEquals(1, size));
    }
}
