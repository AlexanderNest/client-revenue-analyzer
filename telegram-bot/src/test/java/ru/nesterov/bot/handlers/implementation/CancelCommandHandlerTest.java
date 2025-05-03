package ru.nesterov.bot.handlers.implementation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.bot.handlers.AbstractHandlerTest;
import ru.nesterov.bot.handlers.service.BotHandlersRequestsKeeper;

@ContextConfiguration(classes = {
        CancelCommandHandler.class,
})
public class CancelCommandHandlerTest  extends AbstractHandlerTest {
    @Autowired
    private BotHandlersRequestsKeeper botHandlersRequestsKeeper;
    @Test
    void cancelHandlers() {

    }

}
