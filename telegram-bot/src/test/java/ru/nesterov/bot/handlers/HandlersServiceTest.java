package ru.nesterov.bot.handlers;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.implementation.invocable.GetActiveClientsHandler;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class HandlersServiceTest extends RegisteredUserHandlerTest {

    @MockBean
    private GetActiveClientsHandler mockGetActiveClientsHandler;

    @MockBean
    private List<InvocableCommandHandler> invocableCommandHandlers;

    @Test
    public void shouldReturnTrueWhenMessageMatchesCommand() {
        Update update = createUpdateWithMessage("/Вывести список клиентов");

        when(mockGetActiveClientsHandler.getCommand()).thenReturn("/Вывести список клиентов");
        when(invocableCommandHandlers.stream())
                .thenReturn(Stream.of(mockGetActiveClientsHandler));

        boolean result = handlerService.isCommandUpdate(update);

        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenMessageDoesNotMatchAnyCommand() {
        Update update = createUpdateWithMessage("/unknownCommand");

        when(mockGetActiveClientsHandler.getCommand()).thenReturn("/Вывести список клиентов");
        when(invocableCommandHandlers.stream())
                .thenReturn(Stream.of(mockGetActiveClientsHandler));

        boolean result = handlerService.isCommandUpdate(update);

        assertFalse(result);
    }

    @Test
    public void shouldReturnFalseWhenMessageIsNull() {
        Update update = new Update(); // сообщение null

        boolean result = handlerService.isCommandUpdate(update);

        assertFalse(result);
    }
}


