package ru.nesterov.core.exception;

import ru.nesterov.calendar.integration.dto.EventStatus;

public class UnknownEventStatusException extends CoreException {
    public UnknownEventStatusException(EventStatus eventStatus) {
        super("Обнаружен неизвестный EventStatus [" + eventStatus + "]");
    }
}
