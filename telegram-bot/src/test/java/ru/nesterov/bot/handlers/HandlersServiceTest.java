package ru.nesterov.bot.handlers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.nesterov.bot.handlers.abstractions.CommandHandler;
import ru.nesterov.bot.handlers.abstractions.InvocableCommandHandler;
import ru.nesterov.bot.handlers.abstractions.Priority;
import ru.nesterov.bot.handlers.abstractions.StatefulCommandHandler;
import ru.nesterov.bot.handlers.implementation.UndefinedHandler;
import ru.nesterov.bot.handlers.implementation.invocable.CancelCommandHandler;
import ru.nesterov.bot.handlers.service.HandlersService;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(classes = {
        HandlersServiceTestConfig.class
})
public class HandlersServiceTest {

    @Autowired
    private HandlersService handlersService;

    @Autowired
    private CancelCommandHandler cancelCommandHandler;

    @Autowired
    private UndefinedHandler undefinedHandler;

    @Test
    public void shouldReturnCancelHandlerForCancelCommand() {
        Update update = createUpdateWithMessage("/cancel");

        CommandHandler handler = handlersService.getHandler(update);

        assertNotNull(handler);
        assertEquals(cancelCommandHandler, handler);
    }

    @Test
    public void shouldReturnHandlerBasedOnPriority() {
        CommandHandler highPriorityHandler = mock(CommandHandler.class);
        when(highPriorityHandler.getPriority()).thenReturn(Priority.HIGHEST);
        when(highPriorityHandler.isApplicable(any())).thenReturn(false); // true

        CommandHandler normalPriorityHandler = mock(CommandHandler.class);
        when(normalPriorityHandler.getPriority()).thenReturn(Priority.NORMAL);
        when(normalPriorityHandler.isApplicable(any())).thenReturn(true);

        CommandHandler lowestPriorityHandler = mock(CommandHandler.class);
        when(lowestPriorityHandler.getPriority()).thenReturn(Priority.LOWEST);
        when(lowestPriorityHandler.isApplicable(any())).thenReturn(true);

        HandlersService service = new HandlersService(
                List.of(highPriorityHandler, normalPriorityHandler, lowestPriorityHandler),
                List.of(),
                undefinedHandler,
                cancelCommandHandler,
                List.of()
        );

        Update update = createUpdateWithMessage("Вывести список клиентов");

        CommandHandler result = service.getHandler(update);

        assertEquals(normalPriorityHandler, result); //highPriorityHandler
    }


    @Test
    public void shouldResetContextEvenWhenNoHandlerForCommand() {
        // Arrange
        Long userId = 1L;

        StatefulCommandHandler<?, ?> activeHandler = mock(StatefulCommandHandler.class);
        when(activeHandler.isFinishedOrNotStarted(userId)).thenReturn(true);
        when(activeHandler.getCommand()).thenReturn("Добавить клиента");
        when(activeHandler.isApplicable(any(Update.class))).thenReturn(true);

        // Создаем команду, для которой нет обработчика
        InvocableCommandHandler unknownCommand = mock(InvocableCommandHandler.class);
        when(unknownCommand.getCommand()).thenReturn("/unknown");
        when(unknownCommand.isApplicable(any(Update.class))).thenReturn(true);

        HandlersService service = new HandlersService(
                List.of(),
                List.of(activeHandler),
                undefinedHandler,
                cancelCommandHandler,
                List.of(unknownCommand)
        );

        Update update = createUpdateWithMessage("/unknown");

        CommandHandler result = service.getHandler(update);

        verify(activeHandler).resetState(userId);

        assertEquals(undefinedHandler, result);

        assertTrue(service.isCommandUpdate(update));
    }

    @Test
    public void shouldResetContextAndTakeControlWhenCommandReceived() {
        // Arrange
        Long userId = 1L;

        // Создаем активный stateful обработчик (имитируем, что он в процессе работы)
        StatefulCommandHandler<?, ?> activeHandler = mock(StatefulCommandHandler.class);
        when(activeHandler.isFinishedOrNotStarted(userId)).thenReturn(true);
        when(activeHandler.isApplicable(any())).thenReturn(true);

        // Создаем команду, которая должна перехватить управление
        CommandHandler commandHandler = mock(CommandHandler.class);
        when(commandHandler.getPriority()).thenReturn(Priority.NORMAL);
        when(commandHandler.isApplicable(any())).thenReturn(true);

        // Создаем invocable команду (чтобы isCommandUpdate возвращал true)
        InvocableCommandHandler invocableCommand = mock(InvocableCommandHandler.class);
        when(invocableCommand.getCommand()).thenReturn("/newcommand");
        when(invocableCommand.isApplicable(any())).thenReturn(true);

        HandlersService service = new HandlersService(
                List.of(commandHandler),
                List.of(activeHandler),
                undefinedHandler,
                cancelCommandHandler,
                List.of(invocableCommand)
        );

        Update update = createUpdateWithMessage("/newcommand");

        // Act
        CommandHandler result = service.getHandler(update);

        // Assert
        // Проверяем, что активный обработчик был сброшен
        verify(activeHandler).resetState(userId);

        // Проверяем, что управление перехвачено новым обработчиком команды
        assertEquals(commandHandler, result);

        // Проверяем, что это действительно команда
        assertTrue(service.isCommandUpdate(update));
    }

    private Update createUpdateWithMessage(String text) {
        Chat chat = new Chat();
        chat.setId(1L);
        User user = new User();
        user.setId(1L);

        Message message = new Message();
        message.setText(text);
        message.setChat(chat);
        message.setFrom(user);

        Update update = new Update();
        update.setMessage(message);

        return update;
    }

}