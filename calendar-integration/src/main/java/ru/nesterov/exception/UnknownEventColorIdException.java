package ru.nesterov.exception;

import com.google.api.client.util.DateTime;
import ru.nesterov.common.exception.AppException;

public class UnknownEventColorIdException extends AppException {
    public UnknownEventColorIdException(String colorId, String summary, DateTime eventStart) {
        super("Неизвестный eventColorId [" + colorId + "] у EventDto [" + summary + "] с датой [" + eventStart + "]");
    }
}
