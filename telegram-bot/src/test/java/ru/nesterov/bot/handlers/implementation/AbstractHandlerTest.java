package ru.nesterov.bot.handlers.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.bot.handlers.BotHandlersRequestsKeeper;
import ru.nesterov.bot.handlers.HandlersService;
import ru.nesterov.calendar.InlineCalendarBuilder;
import ru.nesterov.integration.ClientRevenueAnalyzerIntegrationClient;

/**
 * Базовый тест для Handler. Содержит основные бины, которые используют обработчики.
 */
@ContextConfiguration(classes = {
        ObjectMapper.class,
        BotHandlersRequestsKeeper.class,
        InlineCalendarBuilder.class,
        HandlersService.class
})
@SpringBootTest
public abstract class AbstractHandlerTest {
    @Autowired
    protected BotHandlersRequestsKeeper keeper;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected HandlersService handlerService;

    @MockBean
    protected ClientRevenueAnalyzerIntegrationClient client;
}
