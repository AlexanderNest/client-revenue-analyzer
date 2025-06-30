package ru.nesterov.exception;

import com.google.api.services.calendar.model.EventDateTime;

public class CannotBuildEventIntegrationException extends CalendarIntegrationException {
    public CannotBuildEventIntegrationException(String summary, EventDateTime dateTime, Throwable cause) {
        super("Не удалось собрать EventDto [" + summary+ "] с датой [" + dateTime + "]", cause);
    }

    public CannotBuildEventIntegrationException(String summary, EventDateTime dateTime) {
        super("Не удалось собрать EventDto [" + summary+ "] с датой [" + dateTime + "]");
    }
}
