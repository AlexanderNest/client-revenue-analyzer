package ru.nesterov.bot.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.implementation.AiAnalyzerHandler;
import ru.nesterov.bot.handlers.implementation.CancelCommandHandler;
import ru.nesterov.bot.handlers.implementation.CreateClientHandler;
import ru.nesterov.bot.handlers.implementation.CreateUserHandler;
import ru.nesterov.bot.handlers.implementation.GetActiveClientsHandler;
import ru.nesterov.bot.handlers.implementation.GetClientScheduleCommandHandler;
import ru.nesterov.bot.handlers.implementation.GetMonthStatisticsCommandHandler;
import ru.nesterov.bot.handlers.implementation.GetYearBusynessStatisticsHandler;
import ru.nesterov.bot.handlers.implementation.MakeEventsBackupHandler;
import ru.nesterov.bot.handlers.implementation.UndefinedHandler;
import ru.nesterov.bot.handlers.implementation.UpdateUserControlButtonsHandler;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.properties.BotProperties;
import ru.nesterov.properties.RevenueAnalyzerProperties;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ContextConfiguration(classes = {
        AiAnalyzerHandler.class,
        CancelCommandHandler.class,
        CreateClientHandler.class,
        CreateUserHandler.class,
        GetActiveClientsHandler.class,
        GetClientScheduleCommandHandler.class,
        GetMonthStatisticsCommandHandler.class,
        GetYearBusynessStatisticsHandler.class,
        MakeEventsBackupHandler.class,
        UndefinedHandler.class,
        UpdateUserControlButtonsHandler.class,
        ObjectMapper.class,
        ClientRevenueAnalyzerIntegrationClient.class,
        RestTemplate.class,
        BotHandlersRequestsKeeper.class,
        RevenueAnalyzerProperties.class,
        BotProperties.class,
        InlineCalendarBuilder.class})

public class InvocableHandlersTest{
    @Autowired
    private List<InvocableCommandHandler> handlers;

    @Test
    public void allHandlersCommandIsNotEmpty() {
        assertFalse(handlers.isEmpty(), "Handlers list should not be empty");

    for (InvocableCommandHandler handler : handlers) {
        assertNotNull(handler, "Handler should not be null");
        String command = handler.getCommand();

        assertNotNull(command, "Команда не должна быть null для handler: " + handler.getClass().getSimpleName());
        assertFalse(command.trim().isEmpty(), "Команда не должна быть пустой или состоять из пробелов для handler: " + handler.getClass().getSimpleName());
    }
    }
}
