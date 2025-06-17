package ru.nesterov.exception;

import ru.nesterov.common.exception.AppException;
import ru.nesterov.dto.EventStatus;

public class UnknownEventStatusException extends AppException {
    public UnknownEventStatusException(EventStatus eventStatus) {
        super("Обнаружен неизвестный EventStatus [" + eventStatus + "]");
    }
}
