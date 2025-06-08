package ru.nesterov.bot.exception;

/**
 * Исключение, которое не будет отображаться для пользователя, но будет залогировано
 */
public class InternalException extends RuntimeException {
    public InternalException(Throwable cause, String message) {
        super(message, cause);
    }

    public InternalException(Throwable cause) {
        super(cause);
    }
}
