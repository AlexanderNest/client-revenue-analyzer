package ru.nesterov.calendar.integration.exception;

public class CalendarIntegrationException extends RuntimeException {
    public CalendarIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalendarIntegrationException(String message) {
        super(message);
    }
}
