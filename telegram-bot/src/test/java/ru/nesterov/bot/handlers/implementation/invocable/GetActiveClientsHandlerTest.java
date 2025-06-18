package ru.nesterov.bot.handlers.implementation.invocable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        GetActiveClientsHandler.class
})
class GetActiveClientsHandlerTest extends RegisteredUserHandlerTest {
    @Autowired
    private GetActiveClientsHandler getActiveClientsHandler;

    @Test
    void handle() {
        Update update = createUpdateWithMessage(getActiveClientsHandler.getCommand());
        CommandHandler commandHandler = handlerService.getHandler(update);
        assertInstanceOf(GetActiveClientsHandler.class, commandHandler);

        GetActiveClientResponse clientResponse = new GetActiveClientResponse();
        clientResponse.setName("Макс");
        clientResponse.setPricePerHour(999);
        clientResponse.setDescription("SSS");

        GetActiveClientResponse clientResponse2 = new GetActiveClientResponse();
        clientResponse2.setName("Анна");
        clientResponse2.setPricePerHour(100);
        clientResponse2.setDescription("Zzz");

        List<GetActiveClientResponse> getActiveClientsResponseList = List.of(clientResponse, clientResponse2);

        when(client.getActiveClients(1L)).thenReturn(getActiveClientsResponseList);

        SendMessage result = (SendMessage) commandHandler.handle(update);

        String expectedMessage = (
                "1. Макс" + System.lineSeparator() +
                "     Тариф: 999 руб/час" + System.lineSeparator() +
                "     Описание: SSS" + System.lineSeparator() +
                "2. Анна" + System.lineSeparator() +
                "     Тариф: 100 руб/час" + System.lineSeparator() +
                "     Описание: Zzz");

        assertEquals(expectedMessage, result.getText());
    }
}
