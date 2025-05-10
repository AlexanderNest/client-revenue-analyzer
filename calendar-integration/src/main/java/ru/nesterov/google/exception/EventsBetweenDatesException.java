package ru.nesterov.google.exception;

import ru.nesterov.exception.AppException;

public class EventsBetweenDatesException extends AppException {
    public EventsBetweenDatesException(String message) {
        super(message);
    }
}
