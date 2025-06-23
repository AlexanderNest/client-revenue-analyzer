package ru.nesterov.bot.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.implementation.UndefinedHandler;
import ru.nesterov.bot.handlers.implementation.invocable.CancelCommandHandler;
import ru.nesterov.bot.handlers.service.HandlersService;
import ru.nesterov.bot.integration.ClientRevenueAnalyzerIntegrationClient;

import java.util.List;

public class HandlersServiceTestConfig {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public CancelCommandHandler cancelCommandHandler() {
        return new CancelCommandHandler(/* зависимости если есть */);
    }

    @Bean
    public UndefinedHandler undefinedHandler() {
        return new UndefinedHandler();
    }

    @Bean
    public List<CommandHandler> commandHandlers(CancelCommandHandler cancelCommandHandler) {
        return List.of(cancelCommandHandler);
    }
    @Bean
    public ClientRevenueAnalyzerIntegrationClient clientRevenueAnalyzerIntegrationClient() {
        return Mockito.mock(ClientRevenueAnalyzerIntegrationClient.class);
    }

    @Bean
    public List<StatefulCommandHandler<?, ?>> statefulCommandHandlers() {
        return List.of();
    }

    @Bean
    public List<InvocableCommandHandler> invocableCommandHandlers(
            CancelCommandHandler cancelCommandHandler) {
        return List.of(cancelCommandHandler);
    }

    @Bean
    public HandlersService handlersService(
            List<CommandHandler> commandHandlers,
            List<StatefulCommandHandler<?, ?>> statefulCommandHandlers,
            UndefinedHandler undefinedHandler,
            CancelCommandHandler cancelCommandHandler,
            List<InvocableCommandHandler> invocableCommandHandlers) {
        return new HandlersService(
                commandHandlers,
                statefulCommandHandlers,
                undefinedHandler,
                cancelCommandHandler,
                invocableCommandHandlers);
    }
}

