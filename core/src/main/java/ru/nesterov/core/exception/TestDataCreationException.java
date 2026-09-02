package ru.nesterov.core.exception;

public class TestDataCreationException extends CoreException {
    public TestDataCreationException(String username, Throwable cause) {
        super("Не удалось создать тестовые данные для пользователя [" + username + "]: " + cause.getMessage(), cause);
    }
}
