package ru.nesterov.exception;

import ru.nesterov.common.dto.EventStatus;
import ru.nesterov.common.exception.AppException;

public class UnknownEventStatusException extends AppException {
    public UnknownEventStatusException(EventStatus eventStatus) {
        super("Обнаружен неизвестный EventStatus [" + eventStatus + "]");
    }
}
