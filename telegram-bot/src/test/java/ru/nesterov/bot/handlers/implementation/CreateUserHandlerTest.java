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
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        Message message = new Message();
        message.setText("/register");
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        BotApiMethod<?> botApiMethod = createUserHandler.handle(update);

        assertTrue(botApiMethod instanceof SendMessage);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Чтобы зарегистрироваться в Анализаторе клиентов, понадобится " +
                "id основного календаря и календаря, в котором будут сохраняться отмененные мероприятия.\n\n Пришлите id основного календаря: ", sendMessage.getText());

        message.setText("12345mc");
        update.setMessage(message);

        botApiMethod = createUserHandler.handle(update);
        sendMessage = (SendMessage) botApiMethod;
        assertEquals("Пришлите id календаря, в котором будут храниться отмененные мероприятия: ", sendMessage.getText());
    }
}
