package ru.nesterov.bot.handlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.bot.handlers.implementation.CancelCommandHandler;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;
import ru.nesterov.bot.handlers.service.ButtonCallbackService;
import ru.nesterov.bot.handlers.service.HandlersService;
import ru.nesterov.bot.handlers.wrapper.UpdateUserControlButtonsHandlerWrapper;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;
import ru.nesterov.properties.BotProperties;

/**
 * Базовый тест для Handler. Содержит основные бины, которые используют обработчики.
 */
@ContextConfiguration(classes = {
        ObjectMapper.class,
        BotHandlersRequestsKeeper.class,
        InlineCalendarBuilder.class,
        HandlersService.class,
        ButtonCallbackService.class,
        BotProperties.class,
        CancelCommandHandler.class,
        UpdateUserControlButtonsHandlerWrapper.class
})
@SpringBootTest
public abstract class AbstractHandlerTest {
    @Autowired
    protected BotHandlersRequestsKeeper keeper;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected HandlersService handlerService;
    @Autowired
    protected ButtonCallbackService buttonCallbackService;
    @Autowired
    protected BotProperties botProperties;
    @Autowired
    protected CancelCommandHandler cancelCommandHandler;

    @MockBean
    protected ClientRevenueAnalyzerIntegrationClient client;
}
