package ru.nesterov.bot.handlers;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ComponentScan("ru.nesterov.bot.handlers.implementation")
public class InvocableHandlersTest extends AbstractHandlerTest {
    @Autowired
    private List<InvocableCommandHandler> handlers;
    @Test
    public void allHandlersCommandIsNotEmpty() {
        assertFalse(handlers.isEmpty(), "Handlers list should not be empty");

        for (InvocableCommandHandler handler : handlers) {
            assertNotNull(handler, "Handler should not be null");

            String command = handler.getCommand();
            assertTrue(StringUtils.isNotBlank(command), "Команда + '" + command + "' не должна быть пустой или состоять из пробелов для handler: " + handler.getClass().getSimpleName());
        }
    }
}
