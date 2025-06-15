package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.nesterov.bot.dto.AiAnalyzerResponse;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.implementation.invocable.AiAnalyzerHandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        AiAnalyzerHandler.class
})
class AiAnalyzerHandlerTest extends RegisteredUserHandlerTest {
    @Test
    void handle() {
        Update update = createUpdateWithMessage("Анализ клиентов ИИ");
        CommandHandler commandHandler = handlerService.getHandler(update);

        assertInstanceOf(AiAnalyzerHandler.class, commandHandler);

        AiAnalyzerResponse aiAnalyzerResponse = new AiAnalyzerResponse();
        aiAnalyzerResponse.setContent("Клиент 1 показал отличные результаты в плане продуктивности и дохода.");
        when(client.getAiStatistics(anyLong())).thenReturn(aiAnalyzerResponse);

        BotApiMethod<?> command = commandHandler.handle(update);
        assertInstanceOf(SendMessage.class, command);
        SendMessage sendStatistics = (SendMessage) command;

        assertEquals(aiAnalyzerResponse.getContent(), sendStatistics.getText());
    }

    private Update createUpdateWithMessage(String text) {
        Chat chat = new Chat();
        chat.setId(1L);
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setText(text);
        message.setChat(chat);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }
}
