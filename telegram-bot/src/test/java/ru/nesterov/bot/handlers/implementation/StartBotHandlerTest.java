package ru.nesterov.bot.handlers.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.properties.BotProperties;


@SpringBootTest
@ContextConfiguration(classes = {
        GetClientScheduleCommandHandler.class,
        GetMonthStatisticsCommandHandler.class,
        ObjectMapper.class,
        BotHandlersRequestsKeeper.class,
        InlineCalendarBuilder.class,
        StartBotHandler.class,
        BotProperties.class
})
@EnableConfigurationProperties(BotProperties.class)
public class StartBotHandlerTest {
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;

    @Autowired
    private StartBotHandler startBotHandler;

    @Test
    public void test() {
        Update update = new Update();
        Message message = new Message();
        Chat chat = new Chat();
        chat.setId(111L);
        message.setChat(chat);
        message.setText("/start");
        update.setMessage(message);

        BotApiMethod<?> result = startBotHandler.handle(update);

        assertNotNull(result);
        assertEquals(SendMessage.class, result.getClass());

        SendMessage sendMessage = (SendMessage) result;
        assertEquals("111", sendMessage.getChatId());
        assertEquals("Выберете опцию:", sendMessage.getText());

        ReplyKeyboardMarkup replyKeyboardMarkup = (ReplyKeyboardMarkup) sendMessage.getReplyMarkup();
        assertNotNull(replyKeyboardMarkup);
        assertNotNull(replyKeyboardMarkup.getKeyboard());
    }
}
