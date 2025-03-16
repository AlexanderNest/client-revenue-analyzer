package ru.nesterov.google.exception;

import com.google.api.services.calendar.model.EventDateTime;
import ru.nesterov.exception.AppException;

public class CannotBuildEventException extends AppException {
    public CannotBuildEventException(String summary, EventDateTime dateTime, Throwable cause) {
        super("Не удалось собрать EventDto [" + summary+ "] с датой [" + dateTime + "]", cause);
    }

    public CannotBuildEventException(String summary, EventDateTime dateTime) {
        this(summary, dateTime, null);
    }
}
