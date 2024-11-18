package ru.nesterov.bot.handlers;


import org.springframework.boot.test.mock.mockito.MockBean;
import ru.nesterov.bot.handlers.implementation.UnregisteredUserHandler;

/**
 * Базовый тест для Handler для работы с зарегистрированным пользователем. Содержит основные бины, которые используют обработчики.
 */
public abstract class RegisteredUserHandler extends AbstractHandlerTest {
    @MockBean
    protected UnregisteredUserHandler unregisteredUserHandler;
}
