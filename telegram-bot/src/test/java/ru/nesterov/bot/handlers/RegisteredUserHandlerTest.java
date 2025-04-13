package ru.nesterov.bot.handlers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import ru.nesterov.bot.handlers.implementation.CreateUserHandler;
import ru.nesterov.bot.handlers.implementation.UpdateUserControlButtonsHandler;
import ru.nesterov.bot.handlers.service.ButtonCallbackService;

/**
 * Базовый тест для Handler для работы с зарегистрированным пользователем. Содержит основные бины, которые используют обработчики.
 */
@ContextConfiguration(classes = {
        ButtonCallbackService.class,
        UpdateUserControlButtonsHandler.class,
        CreateUserHandler.class
})
public abstract class RegisteredUserHandlerTest extends AbstractHandlerTest {
    @Autowired
    protected UpdateUserControlButtonsHandler updateUserControlButtonsHandler;
}
