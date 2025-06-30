package ru.nesterov.exception;

import com.google.api.client.util.DateTime;

public class UnknownEventColorIdIntegrationException extends CalendarIntegrationException {
    public UnknownEventColorIdIntegrationException(String colorId, String summary, DateTime eventStart) {
        super("Неизвестный eventColorId [" + colorId + "] у EventDto [" + summary + "] с датой [" + eventStart + "]");
    }
}
