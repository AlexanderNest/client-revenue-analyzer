package ru.nesterov.bot.handlers.implementation;


import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Базовый тест для Handler для работы с зарегистрированным пользователем. Содержит основные бины, которые используют обработчики.
 */
public abstract class RegisteredUserHandler extends AbstractHandlerTest {
    @MockBean
    protected UnregisteredUserHandler unregisteredUserHandler;
}
