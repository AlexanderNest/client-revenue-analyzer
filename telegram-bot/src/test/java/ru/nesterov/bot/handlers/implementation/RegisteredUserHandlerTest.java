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
import org.telegram.telegrambots.meta.api.objects.User;
import ru.nesterov.bot.handlers.AbstractHandlerTest;
import ru.nesterov.dto.GetUserRequest;
import ru.nesterov.properties.BotProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        UpdateUserControlButtonsHandler.class,
        CreateUserHandler.class,
        GetClientScheduleCommandHandler.class,
        GetMonthStatisticsCommandHandler.class,
        BotProperties.class
})
@EnableConfigurationProperties(BotProperties.class)
@TestPropertySource(properties = {
        "bot.menu-buttons-per-line=1"
})
public class RegisteredUserHandlerTest extends AbstractHandlerTest {
    @Autowired
    private UpdateUserControlButtonsHandler updateUserControlButtonsHandler;
    @Autowired
    private CreateUserHandler createUserHandler;

    @Test
    void handleRegisteredUserUpdate() {
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

        BotApiMethod<?> ourUser = createUserHandler.handle(update);

        when(client.getUserByUsername(any(GetUserRequest.class))).thenReturn(ourUser, BotApiMethod.class);

        BotApiMethod<?> botApiMethod = updateUserControlButtonsHandler.handle(update);

        assertInstanceOf(SendMessage.class, botApiMethod);

        SendMessage sendMessage = (SendMessage) botApiMethod;
        assertEquals("Выберите опцию:", sendMessage.getText());
    }
}
