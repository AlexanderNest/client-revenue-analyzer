package ru.nesterov.bot.handlers.implementation.invocable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.dto.GetActiveClientResponse;
import ru.nesterov.bot.handlers.RegisteredUserHandlerTest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {
        GetActiveClientsHandler.class
})
class GetActiveClientsHandlerTest extends RegisteredUserHandlerTest {
    @Autowired
    private GetActiveClientsHandler getActiveClientsHandler;

    @Test
    void handleShouldReturnFormattedMessage() {
        Update update = createUpdateWithMessage(getActiveClientsHandler.getCommand());

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

        SendMessage result = (SendMessage) getActiveClientsHandler.handle(update);

        String expectedMessage =
                "1. Макс" + System.lineSeparator() +
                "     Тариф: 999 руб/час" + System.lineSeparator() +
                "     Описание: SSS" + System.lineSeparator() + System.lineSeparator() +
                "2. Анна" + System.lineSeparator() +
                "     Тариф: 100 руб/час" + System.lineSeparator() +
                "     Описание: Zzz" + System.lineSeparator() + System.lineSeparator();

        assertEquals(expectedMessage, result.getText());
    }

    @Test
    void handleShouldReturnNoClientsMessage() {
        Update update = createUpdateWithMessage(getActiveClientsHandler.getCommand());

        when(client.getActiveClients(1L)).thenReturn(Collections.emptyList());

        SendMessage result = (SendMessage) getActiveClientsHandler.handle(update);

        assertEquals("У вас пока нет клиентов.", result.getText());
    }
    @Test
    void handleShouldSortClientsByName(){
        Update update = createUpdateWithMessage(getActiveClientsHandler.getCommand());

        GetActiveClientResponse client1 = new GetActiveClientResponse();
        client1.setName("Яна");
        client1.setPricePerHour(500);
        client1.setDescription("1я");

        GetActiveClientResponse client2 = new GetActiveClientResponse();
        client2.setName("Андрей");
        client2.setPricePerHour(600);
        client2.setDescription("2й");

        GetActiveClientResponse client3 = new GetActiveClientResponse();
        client3.setName("борис");
        client3.setPricePerHour(700);
        client3.setDescription("3й");

        List<GetActiveClientResponse> getActiveClientsResponseList = List.of(client1,client2,client3);

        when(client.getActiveClients(1L)).thenReturn(getActiveClientsResponseList);

        SendMessage resultSort = (SendMessage) getActiveClientsHandler.handle(update);

        String expectedMessage =
                "1. Андрей"  + System.lineSeparator() +
                "     Тариф: 600 руб/час"  + System.lineSeparator() +
                "     Описание: 2й" + System.lineSeparator() + System.lineSeparator() +

                "2. борис" + System.lineSeparator() +
                "     Тариф: 700 руб/час" + System.lineSeparator() +
                "     Описание: 3й"  + System.lineSeparator() + System.lineSeparator() +

                "3. Яна" + System.lineSeparator() +
                "     Тариф: 500 руб/час"  + System.lineSeparator() +
                "     Описание: 1я"  + System.lineSeparator() + System.lineSeparator();

        assertEquals(expectedMessage, resultSort.getText());
    }
}
