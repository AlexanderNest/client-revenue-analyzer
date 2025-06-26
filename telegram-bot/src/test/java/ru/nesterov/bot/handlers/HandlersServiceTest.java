package ru.nesterov.bot.handlers;

import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.implementation.CreateClientTest;
import ru.nesterov.bot.handlers.implementation.UpdateUserControlButtonsHandlerTest;
import ru.nesterov.bot.handlers.implementation.invocable.AiAnalyzerHandler;
import ru.nesterov.bot.handlers.implementation.invocable.CancelCommandHandler;
import ru.nesterov.bot.handlers.implementation.invocable.GetActiveClientsHandler;
import ru.nesterov.bot.handlers.implementation.invocable.GetMonthStatisticsCommandHandler;
import ru.nesterov.bot.handlers.implementation.invocable.GetUnpaidEventsHandler;
import ru.nesterov.bot.handlers.implementation.invocable.UpdateUserControlButtonsHandler;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.createClient.CreateClientHandler;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.createUser.CreateUserHandler;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.getSchedule.GetClientScheduleCommandHandler;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.getYearBusynessStatistics.GetYearBusynessStatisticsHandler;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.makeEventsBackupHandler.MakeEventsBackupHandler;
import ru.nesterov.bot.handlers.service.HandlersService;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



/*public class HandlersServiceTest extends RegisteredUserHandlerTest{

    @MockBean
    private CreateClientHandler createClientHandler;

    @MockBean
    private CancelCommandHandler cancelCommandHandler;

    @MockBean
    private GetActiveClientsHandler mockGetActiveClientsHandler;

    @MockBean
    private List<InvocableCommandHandler> invocableCommandHandlers;

    @MockBean
    private CreateUserHandler createUserHandler;

    @MockBean
    private UpdateUserControlButtonsHandler updateUserControlButtonsHandler;

    @Test
    public void WhenMessageTextMatchesCommandHandler() {
        Update update = createUpdateWithMessage("/Вывести список клиентов");

        when(mockGetActiveClientsHandler.getCommand()).thenReturn("/Вывести список клиентов");
        when(mockGetActiveClientsHandler.isApplicable(update)).thenReturn(true);
        // Настраиваем мок списка обработчиков
        when(invocableCommandHandlers.stream()).thenReturn(Stream.of(mockGetActiveClientsHandler));

        when(createClientHandler.getCommand()).thenReturn("/create_client");
        when(createClientHandler.isApplicable(update)).thenReturn(false);

        when(cancelCommandHandler.getCommand()).thenReturn("/cancel");
        when(cancelCommandHandler.isApplicable(update)).thenReturn(false);

        when(createUserHandler.getCommand()).thenReturn("/create_user");
        when(createUserHandler.isApplicable(update)).thenReturn(false);

        List<InvocableCommandHandler> handlersList = Arrays.asList(
                mockGetActiveClientsHandler,
                createClientHandler,
                cancelCommandHandler,
                createUserHandler
        );

        when(invocableCommandHandlers.stream()).thenReturn(handlersList.stream());

        boolean result = handlerService.isCommandUpdate(update);
        assertTrue(result);

    }
    @Test
    public void shouldReturnFalseWhenMessageTextDoesNotMatchCommand() {
        Update update = createUpdateWithMessage("/unknownCommand");

        // Настраиваем моки обработчиков
        when(mockGetActiveClientsHandler.getCommand()).thenReturn("/Вывести список клиентов");
        when(mockGetActiveClientsHandler.isApplicable(update)).thenReturn(false);

        when(createClientHandler.getCommand()).thenReturn("/create_client");
        when(createClientHandler.isApplicable(update)).thenReturn(false);

        when(cancelCommandHandler.getCommand()).thenReturn("/cancel");
        when(cancelCommandHandler.isApplicable(update)).thenReturn(false);

        when(createUserHandler.getCommand()).thenReturn("/create_user");
        when(createUserHandler.isApplicable(update)).thenReturn(false);

        when(updateUserControlButtonsHandler.getCommand()).thenReturn("/updateUserControl");
        when(updateUserControlButtonsHandler.isApplicable(update)).thenReturn(false);

        // Создаем список всех обработчиков
        List<InvocableCommandHandler> handlersList = Arrays.asList(
                mockGetActiveClientsHandler,
                createClientHandler,
                cancelCommandHandler,
                createUserHandler,
                updateUserControlButtonsHandler
        );

        // Мокаем поток обработчиков
        when(invocableCommandHandlers.stream()).thenReturn(handlersList.stream());

        boolean result = handlerService.isCommandUpdate(update);

        assertFalse(result);
        }
    }*/

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


