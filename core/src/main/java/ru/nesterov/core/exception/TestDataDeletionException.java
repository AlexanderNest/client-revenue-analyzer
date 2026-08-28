package ru.nesterov.core.exception;

public class TestDataDeletionException extends CoreException {
    public TestDataDeletionException(String username, Throwable cause) {
        super("Не удалось удалить тестовые данные для пользователя [" + username + "]: " + cause.getMessage(), cause);
    }
}
