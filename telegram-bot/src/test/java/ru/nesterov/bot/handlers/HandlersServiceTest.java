package ru.nesterov.bot.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;

import ru.nesterov.bot.handlers.implementation.UndefinedHandler;
import ru.nesterov.bot.handlers.implementation.invocable.CancelCommandHandler;
import ru.nesterov.bot.handlers.implementation.invocable.stateful.createClient.CreateClientHandler;
import ru.nesterov.bot.handlers.service.HandlersService;

import java.util.List;


@ContextConfiguration(classes ={
        HandlersService.class,
        UndefinedHandler.class,
        CancelCommandHandler.class,
        CreateClientHandler.class
        })
public class HandlersServiceTest extends AbstractHandlerTest {

        @Autowired
        private HandlersService handlersService;

        @Autowired
        UndefinedHandler undefinedHandler;

        @Autowired
        CancelCommandHandler cancelCommandHandler;

        @Autowired
        CreateClientHandler createClientHandler;

        @MockBean
        private List<StatefulCommandHandler<?, ?>> statefulCommandHandlers;

        @Test



}
