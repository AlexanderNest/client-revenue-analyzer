package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ContextConfiguration(classes = {UnregisteredUserHandler.class,
        CreateUserHandler.class,
        BotHandlersRequestsKeeper.class,
        ObjectMapper.class
})
public class UnregisteredUserHandlerTest {
    @Autowired
    private UnregisteredUserHandler unregisteredUserHandler;
    @Autowired
    private CreateUserHandler createUserHandler;
    @Autowired
    private BotHandlersRequestsKeeper keeper;
    @MockBean
    private ClientRevenueAnalyzerIntegrationClient client;

    @Test
    void handleUnregisteredUserUpdate() {
        Chat chat = new Chat();
        chat.setId(4L);

        User user = new User();
        user.setId(4L);

        Message message = new Message();
        message.setText("some text");
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        when(client.getUserByUsername(any(GetUserRequest.class))).thenReturn(null);

        BotApiMethod<?> botApiMethod = unregisteredUserHandler.handle(update);

        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Воспользуйтесь командой Зарегистрироваться в боте", sendMessage.getText());
    }
}
