package ru.nesterov.bot.handlers;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.bot.handlers.implementation.CreateUserHandler;
import ru.nesterov.bot.handlers.implementation.UnregisteredUserHandler;
import ru.nesterov.bot.handlers.service.ButtonCallbackService;

/**
 * Базовый тест для Handler для работы с зарегистрированным пользователем. Содержит основные бины, которые используют обработчики.
 */
@ContextConfiguration(classes = {
        ButtonCallbackService.class
})
public abstract class RegisteredUserHandlerTest extends AbstractHandlerTest {
    @MockBean
    protected UnregisteredUserHandler unregisteredUserHandler;
}
