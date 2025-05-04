package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.nesterov.bot.handlers.AbstractHandlerTest;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.service.HandlersService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@ContextConfiguration(classes = {
        HandlersService.class,
        CreateClientHandler.class
})
public class CancelCommandHandlerTest  extends AbstractHandlerTest {
    @Autowired
    private HandlersService handlersService;
    @Autowired
    private CreateClientHandler createClientHandler;

//    private String COMMAND;
//    @BeforeEach
//    public void setUp() {
//        COMMAND = createClientHandler.getCommand();
//    }
    @Test
    void cancelHandlers() {
        /*
        1. кинуть createClient
        2. кинуть еще раз createClient. Это будет считано как ввод имени, проверить что вернули слебующий этап того хендлера
        3. сбросить через /cancel
        4. кинуть createClient, проверить, Что просят имя снова
         */

        SendMessage nameInput = send("Добавить клиента", createClientHandler);
        assertEquals("Введите имя", nameInput.getText());

        SendMessage priceInput = send("", createClientHandler);
        assertEquals("Введите стоимость за час", priceInput.getText());

        SendMessage contextReset = send("/cancel", cancelCommandHandler);
        assertEquals("Контекст сброшен", contextReset.getText());

        SendMessage nameInputAfterReset = send("Добавить клиента", createClientHandler);
        assertEquals("Введите имя", nameInputAfterReset.getText());
    }

    private SendMessage send(String text, CommandHandler commandHandler) {
        Chat chat = new Chat();
        chat.setId(1L);

        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setText(text);
        message.setFrom(user);
        message.setChat(chat);

        Update update = new Update();
        update.setMessage(message);

        BotApiMethod<?> botApiMethod = commandHandler.handle(update);
        assertInstanceOf(SendMessage.class, botApiMethod);

        return (SendMessage) botApiMethod;
    }

//    private SendMessage send() {
//        Chat chat = new Chat();
//        chat.setId(1L);
//
//        User user = new User();
//        user.setId(1L);
//
//        Message message = new Message();
//        message.setText(COMMAND);
//        message.setFrom(user);
//        message.setChat(chat);
//
//        Update update = new Update();
//        update.setMessage(message);
//
//        CommandHandler handler = handlersService.getHandler(update);
//        BotApiMethod<?> botApiMethod = handler.handle(update);
//        assertInstanceOf(SendMessage.class, botApiMethod);
//
//        return (SendMessage) botApiMethod;
//    }
}
