package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.nesterov.bot.RevenueAnalyzerBot;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.implementation.invocable.adminsHandlers.SendMessageToUsersHandler;

import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        SendMessageToUsersHandler.class,
})
public class SendMessageToUsersHandlerTest extends RegisteredUserHandlerTest {
    @Autowired
    private SendMessageToUsersHandler sendMessageToUsersHandler;

    @MockBean
    private RevenueAnalyzerBot revenueAnalyzerBot;

    @Test
    void sendMessage() throws TelegramApiException {
        User user = new User();
        user.setId(1L);

        Chat chat = new Chat();
        chat.setId(1L);

        Message message = new Message();
        message.setText("Рассылка");
        message.setChat(chat);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        List<String> userIds = List.of("100", "150");
        when(client.getUsersIdByRoleAndSource(chat.getId())).thenReturn(userIds);
        BotApiMethod<?> response = sendMessageToUsersHandler.sendMessageToUsers(update);
        for (String userId : userIds) {
            verify(revenueAnalyzerBot).execute((SendMessage) argThat(arg ->
                    arg instanceof SendMessage &&
                            ((SendMessage) arg).getChatId().equals(userId) &&
                            ((SendMessage) arg).getText().equals("Рассылка")
            ));
        }

        Assertions.assertInstanceOf(SendMessage.class, response);
        SendMessage sendMessage = (SendMessage) response;
        Assertions.assertEquals("Рассылка завершена", sendMessage.getText());
    }
}
