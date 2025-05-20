package ru.nesterov.google.exception;

import com.google.api.services.calendar.model.EventDateTime;
import ru.nesterov.common.exception.AppException;

public class UnknownEventColorIdException extends AppException {
    public UnknownEventColorIdException(String colorId, String summary, EventDateTime eventDateTime) {
        super("Неизвестный eventColorId [" + colorId + "] у EventDto [" + summary + "] с датой [" + eventDateTime + "]");
    }
}
