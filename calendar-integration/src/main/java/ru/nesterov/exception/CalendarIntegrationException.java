package ru.nesterov.exception;

public abstract class CalendarIntegrationException extends RuntimeException {
    public CalendarIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalendarIntegrationException(String message) {
        super(message);
    }
}
